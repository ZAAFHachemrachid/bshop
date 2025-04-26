package com.example.b_shop.data.repositories;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.b_shop.data.local.dao.CartDao;
import com.example.b_shop.data.local.relations.CartItemWithProduct;
import com.example.b_shop.data.local.dao.ProductDao;
import com.example.b_shop.data.local.entities.CartItem;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.local.errors.CartError;
import com.example.b_shop.data.local.models.CartOperationResult;
import com.example.b_shop.utils.UserManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CartRepository {
    private final CartDao cartDao;
    private final ProductDao productDao;
    private final UserManager userManager;
    private final ExecutorService executorService;
    private final MutableLiveData<CartError> cartError;

    public CartRepository(CartDao cartDao, ProductDao productDao, UserManager userManager) {
        this.cartDao = cartDao;
        this.productDao = productDao;
        this.userManager = userManager;
        this.cartError = new MutableLiveData<>();
        
        // Create executor with custom error handling
        this.executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r);
            thread.setUncaughtExceptionHandler((t, e) -> {
                android.util.Log.e("CartRepository", "Uncaught exception in cart operation", e);
                CartError error = CartError.databaseError("Unexpected error in cart operation", e);
                cartError.postValue(error);
            });
            return thread;
        });
        
        android.util.Log.d("CartRepository", "CartRepository initialized");
    }

    public LiveData<List<CartItemWithProduct>> getCartItems() {
        android.util.Log.d("CartRepository", "Getting cart items");
        try {
            android.util.Log.d("CartRepository", "Validating user session");
            userManager.validateUserSession();
            int userId = userManager.getCurrentUserId();
            android.util.Log.d("CartRepository", "Fetching cart items for user: " + userId);
            LiveData<List<CartItemWithProduct>> result = cartDao.getCartItemsWithProduct(userId);
            android.util.Log.d("CartRepository", "Cart items LiveData obtained");
            return result;
        } catch (IllegalStateException e) {
            android.util.Log.e("CartRepository", "Session validation failed", e);
            cartError.postValue(CartError.sessionExpired());
            MutableLiveData<List<CartItemWithProduct>> emptyData = new MutableLiveData<>();
            emptyData.postValue(null); // Emit null to trigger state update
            return emptyData;
        }
    }

    public LiveData<Float> getCartTotal() {
        try {
            userManager.validateUserSession();
            return cartDao.getCartTotal(userManager.getCurrentUserId());
        } catch (IllegalStateException e) {
            cartError.postValue(CartError.sessionExpired());
            return new MutableLiveData<>();
        }
    }

    public LiveData<Integer> getCartItemCount() {
        try {
            userManager.validateUserSession();
            return cartDao.getCartItemCount(userManager.getCurrentUserId());
        } catch (IllegalStateException e) {
            cartError.postValue(CartError.sessionExpired());
            return new MutableLiveData<>();
        }
    }

    public LiveData<CartError> getCartError() {
        return cartError;
    }

    public void addToCart(int productId, int quantity, CartOperationCallback callback) {
        android.util.Log.d("CartRepository", "Queuing addToCart operation");
        executorService.execute(() -> {
            try {
                android.util.Log.d("CartRepository", "Starting addToCart: productId=" + productId + ", quantity=" + quantity);
                userManager.validateUserSession();

                if (quantity <= 0) {
                    CartError error = CartError.invalidQuantity(quantity);
                    cartError.postValue(error);
                    callback.onError(error);
                    return;
                }

                Product product = productDao.getProductSync(productId);
                if (product == null) {
                    CartError error = CartError.productNotFound(productId);
                    cartError.postValue(error);
                    callback.onError(error);
                    return;
                }

                android.util.Log.d("CartRepository", "Retrieved product: " + product.getProductId() + ", stock=" + product.getStock());

                CartItem cartItem = new CartItem(
                    userManager.getCurrentUserId(),
                    productId,
                    quantity,
                    product.getPrice()
                );

                android.util.Log.d("CartRepository", "Attempting to validate and add to cart");
                boolean success = cartDao.validateAndAddToCart(cartItem, product);
                
                if (success) {
                    android.util.Log.d("CartRepository", "Successfully added to cart");
                    callback.onSuccess();
                } else {
                    android.util.Log.w("CartRepository", "Failed to add to cart - stock validation failed");
                    CartError error = CartError.insufficientStock(productId, quantity, product.getStock());
                    cartError.postValue(error);
                    callback.onError(error);
                }

            } catch (IllegalStateException e) {
                CartError error = CartError.sessionExpired();
                cartError.postValue(error);
                callback.onError(error);
            } catch (Exception e) {
                CartError error = CartError.databaseError("Failed to add item to cart", e);
                cartError.postValue(error);
                callback.onError(error);
            }
        });
    }

    public void updateQuantity(int cartItemId, int quantity, CartOperationCallback callback) {
        executorService.execute(() -> {
            try {
                userManager.validateUserSession();

                if (quantity <= 0) {
                    CartError error = CartError.invalidQuantity(quantity);
                    cartError.postValue(error);
                    callback.onError(error);
                    return;
                }

                CartItem cartItem = cartDao.getCartItemById(cartItemId);
                if (cartItem == null) {
                    CartError error = CartError.productNotFound(cartItemId);
                    cartError.postValue(error);
                    callback.onError(error);
                    return;
                }

                if (!validateStock(cartItem.getProductId(), quantity)) {
                    Product product = productDao.getProductSync(cartItem.getProductId());
                    CartError error = CartError.insufficientStock(
                        cartItem.getProductId(), 
                        quantity, 
                        product.getStock()
                    );
                    cartError.postValue(error);
                    callback.onError(error);
                    return;
                }

                cartDao.updateQuantity(cartItemId, userManager.getCurrentUserId(), quantity);
                callback.onSuccess();

            } catch (IllegalStateException e) {
                CartError error = CartError.sessionExpired();
                cartError.postValue(error);
                callback.onError(error);
            } catch (Exception e) {
                CartError error = CartError.databaseError("Failed to update quantity", e);
                cartError.postValue(error);
                callback.onError(error);
            }
        });
    }

    public void removeFromCart(int cartItemId, CartOperationCallback callback) {
        executorService.execute(() -> {
            try {
                userManager.validateUserSession();
                cartDao.deleteCartItemById(cartItemId, userManager.getCurrentUserId());
                callback.onSuccess();
            } catch (IllegalStateException e) {
                CartError error = CartError.sessionExpired();
                cartError.postValue(error);
                callback.onError(error);
            } catch (Exception e) {
                CartError error = CartError.databaseError("Failed to remove item from cart", e);
                cartError.postValue(error);
                callback.onError(error);
            }
        });
    }

    public void clearCart(CartOperationCallback callback) {
        executorService.execute(() -> {
            try {
                userManager.validateUserSession();
                cartDao.clearCart(userManager.getCurrentUserId());
                callback.onSuccess();
            } catch (IllegalStateException e) {
                CartError error = CartError.sessionExpired();
                cartError.postValue(error);
                callback.onError(error);
            } catch (Exception e) {
                CartError error = CartError.databaseError("Failed to clear cart", e);
                cartError.postValue(error);
                callback.onError(error);
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
            cartError.postValue(CartError.databaseError("Failed to validate stock", e));
            return false;
        }
    }

    public void cleanup() {
        executorService.shutdown();
    }

    /**
     * Callback interface for cart operations
     */
    public interface CartOperationCallback {
        void onSuccess();
        void onError(CartError error);
    }
}