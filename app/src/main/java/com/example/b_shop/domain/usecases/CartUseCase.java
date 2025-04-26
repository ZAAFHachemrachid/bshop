package com.example.b_shop.domain.usecases;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Transformations;

import com.example.b_shop.data.local.relations.CartItemWithProduct;
import com.example.b_shop.data.repositories.CartRepository;
import com.example.b_shop.data.repositories.ProductRepository;
import com.example.b_shop.utils.UserManager;

import java.util.List;

/**
 * Use case that handles cart-related business logic
 */
public class CartUseCase {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserManager userManager;
    
    private final MediatorLiveData<CartState> cartState;

    public CartUseCase(
        CartRepository cartRepository,
        ProductRepository productRepository,
        UserManager userManager
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userManager = userManager;
        this.cartState = new MediatorLiveData<>();
        
        initializeCartState();
    }

    private void initializeCartState() {
        LiveData<List<CartItemWithProduct>> items = cartRepository.getCartItems();
        LiveData<Float> total = cartRepository.getCartTotal();
        LiveData<Integer> itemCount = cartRepository.getCartItemCount();

        cartState.addSource(items, cartItems -> 
            updateCartState(cartItems, total.getValue(), itemCount.getValue()));
        
        cartState.addSource(total, cartTotal -> 
            updateCartState(items.getValue(), cartTotal, itemCount.getValue()));
        
        cartState.addSource(itemCount, count -> 
            updateCartState(items.getValue(), total.getValue(), count));
    }

    private void updateCartState(
        List<CartItemWithProduct> items,
        Float total,
        Integer count
    ) {
        if (items != null && total != null && count != null) {
            cartState.setValue(new CartState(items, total, count));
        }
    }

    public LiveData<CartState> getCartState() {
        return cartState;
    }

    public void addToCart(int productId, int quantity) {
        try {
            userManager.validateUserSession();
            
            if (validateStock(productId, quantity)) {
                cartRepository.addToCart(productId, quantity);
            } else {
                // TODO: Notify insufficient stock
            }
        } catch (IllegalStateException e) {
            // TODO: Notify user needs to login
        }
    }

    public void updateQuantity(int cartItemId, int quantity) {
        try {
            userManager.validateUserSession();
            
            if (quantity <= 0) {
                cartRepository.removeFromCart(cartItemId);
            } else {
                cartRepository.updateQuantity(cartItemId, quantity);
            }
        } catch (IllegalStateException e) {
            // TODO: Notify user needs to login
        }
    }

    public void removeItem(int cartItemId) {
        try {
            userManager.validateUserSession();
            cartRepository.removeFromCart(cartItemId);
        } catch (IllegalStateException e) {
            // TODO: Notify user needs to login
        }
    }

    public void clearCart() {
        try {
            userManager.validateUserSession();
            cartRepository.clearCart();
        } catch (IllegalStateException e) {
            // TODO: Notify user needs to login
        }
    }

    private boolean validateStock(int productId, int quantity) {
        return cartRepository.validateStock(productId, quantity);
    }

    /**
     * Represents the current state of the shopping cart
     */
    public static class CartState {
        private final List<CartItemWithProduct> items;
        private final float total;
        private final int itemCount;
        private final boolean isEmpty;

        public CartState(List<CartItemWithProduct> items, float total, int itemCount) {
            this.items = items;
            this.total = total;
            this.itemCount = itemCount;
            this.isEmpty = items == null || items.isEmpty();
        }

        public List<CartItemWithProduct> getItems() {
            return items;
        }

        public float getTotal() {
            return total;
        }

        public int getItemCount() {
            return itemCount;
        }

        public boolean isEmpty() {
            return isEmpty;
        }
    }
}