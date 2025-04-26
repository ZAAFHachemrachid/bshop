package com.example.b_shop.domain.usecases;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.b_shop.data.local.relations.CartItemWithProduct;
import com.example.b_shop.data.local.errors.CartError;
import com.example.b_shop.data.repositories.CartRepository;
import com.example.b_shop.data.repositories.CartRepository.CartOperationCallback;
import com.example.b_shop.data.repositories.ProductRepository;
import com.example.b_shop.utils.UserManager;

import java.util.List;

public class CartUseCase {
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserManager userManager;
    
    private final MediatorLiveData<CartState> cartState;
    private final MutableLiveData<CartError> operationError;

    public CartUseCase(
        CartRepository cartRepository,
        ProductRepository productRepository,
        UserManager userManager
    ) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.userManager = userManager;
        this.cartState = new MediatorLiveData<>();
        this.operationError = new MutableLiveData<>();
        
        initializeCartState();
    }

    private void initializeCartState() {
        LiveData<List<CartItemWithProduct>> items = cartRepository.getCartItems();
        LiveData<Float> total = cartRepository.getCartTotal();
        LiveData<Integer> itemCount = cartRepository.getCartItemCount();
        LiveData<CartError> repoError = cartRepository.getCartError();

        cartState.addSource(items, cartItems -> 
            updateCartState(cartItems, total.getValue(), itemCount.getValue()));
        
        cartState.addSource(total, cartTotal -> 
            updateCartState(items.getValue(), cartTotal, itemCount.getValue()));
        
        cartState.addSource(itemCount, count -> 
            updateCartState(items.getValue(), total.getValue(), count));
        
        cartState.addSource(repoError, error -> {
            if (error != null) {
                operationError.setValue(error);
            }
        });
    }

    private void updateCartState(
        List<CartItemWithProduct> items,
        Float total,
        Integer count
    ) {
        android.util.Log.d("CartUseCase", String.format(
            "Updating cart state - Items: %s, Total: %s, Count: %s",
            items != null ? items.size() : "null",
            total != null ? total : "null",
            count != null ? count : "null"
        ));

        // Handle null values gracefully
        List<CartItemWithProduct> safeItems = items != null ? items : List.of();
        float safeTotal = total != null ? total : 0f;
        int safeCount = count != null ? count : 0;

        // Always emit a state update
        android.util.Log.d("CartUseCase", "Emitting new cart state");
        cartState.setValue(new CartState(safeItems, safeTotal, safeCount));
    }

    public LiveData<CartState> getCartState() {
        return cartState;
    }

    public LiveData<CartError> getOperationError() {
        return operationError;
    }

    public void clearError() {
        operationError.setValue(null);
    }

    public void addToCart(int productId, int quantity) {
        cartRepository.addToCart(productId, quantity, new CartOperationCallback() {
            @Override
            public void onSuccess() {
                // State will be updated via LiveData
            }

            @Override
            public void onError(CartError error) {
                operationError.setValue(error);
            }
        });
    }

    public void updateQuantity(int cartItemId, int quantity) {
        cartRepository.updateQuantity(cartItemId, quantity, new CartOperationCallback() {
            @Override
            public void onSuccess() {
                // State will be updated via LiveData
            }

            @Override
            public void onError(CartError error) {
                operationError.setValue(error);
            }
        });
    }

    public void removeItem(int cartItemId) {
        cartRepository.removeFromCart(cartItemId, new CartOperationCallback() {
            @Override
            public void onSuccess() {
                // State will be updated via LiveData
            }

            @Override
            public void onError(CartError error) {
                operationError.setValue(error);
            }
        });
    }

    public void clearCart() {
        cartRepository.clearCart(new CartOperationCallback() {
            @Override
            public void onSuccess() {
                // State will be updated via LiveData
            }

            @Override
            public void onError(CartError error) {
                operationError.setValue(error);
            }
        });
    }

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