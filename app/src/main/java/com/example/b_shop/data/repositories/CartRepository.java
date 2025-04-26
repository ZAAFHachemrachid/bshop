package com.example.b_shop.data.repositories;

import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.dao.CartDao;
import com.example.b_shop.data.local.relations.CartItemWithProduct;
import com.example.b_shop.data.local.dao.ProductDao;
import com.example.b_shop.data.local.entities.CartItem;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.utils.UserManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartRepository {
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final UserManager userManager;
    private final ExecutorService executorService;

    public CartRepository(CartDao cartDao, ProductDao productDao, UserManager userManager) {
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.userManager = userManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<CartItemWithProduct>> getCartItems() {
        return cartDao.getCartItemsWithProduct(userManager.getCurrentUserId());
    }

    public LiveData<Float> getCartTotal() {
        return cartDao.getCartTotal(userManager.getCurrentUserId());
    }

    public LiveData<Integer> getCartItemCount() {
        return cartDao.getCartItemCount(userManager.getCurrentUserId());
    }

    public void addToCart(int productId, int quantity) {
        executorService.execute(() -> {
            try {
                Product product = productDao.getProductSync(productId);
                if (product == null) {
                    // TODO: Handle error - product not found
                    return;
                }

                CartItem cartItem = new CartItem(
                    userManager.getCurrentUserId(),
                    productId,
                    quantity,
                    product.getPrice()
                );

                boolean success = cartDao.validateAndAddToCart(cartItem, product);
                if (!success) {
                    // TODO: Handle error - insufficient stock
                }
            } catch (Exception e) {
                // TODO: Handle error
                e.printStackTrace();
            }
        });
    }

    public void updateQuantity(int cartItemId, int quantity) {
        executorService.execute(() -> {
            try {
                cartDao.updateQuantity(cartItemId, userManager.getCurrentUserId(), quantity);
            } catch (Exception e) {
                // TODO: Handle error
                e.printStackTrace();
            }
        });
    }

    public void removeFromCart(int cartItemId) {
        executorService.execute(() -> {
            try {
                cartDao.deleteCartItemById(cartItemId, userManager.getCurrentUserId());
            } catch (Exception e) {
                // TODO: Handle error
                e.printStackTrace();
            }
        });
    }

    public void clearCart() {
        executorService.execute(() -> {
            try {
                cartDao.clearCart(userManager.getCurrentUserId());
            } catch (Exception e) {
                // TODO: Handle error
                e.printStackTrace();
            }
        });
    }

    public boolean validateStock(int productId, int quantity) {
        try {
            Product product = productDao.getProductSync(productId);
            if (product == null) return false;

            CartItem existingItem = cartDao.getCartItemByProduct(
                userManager.getCurrentUserId(),
                productId
            );

            int totalQuantity = quantity;
            if (existingItem != null) {
                totalQuantity += existingItem.getQuantity();
            }

            return totalQuantity <= product.getStock();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void cleanup() {
        executorService.shutdown();
    }
}