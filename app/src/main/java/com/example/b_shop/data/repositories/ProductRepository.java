package com.example.b_shop.data.repositories;

import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.dao.ProductDao;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.repositories.CartRepository;
import com.example.b_shop.data.repositories.CartRepository.CartOperationCallback;
import com.example.b_shop.utils.UserManager;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {
    private final ProductDao productDao;
    private final CartRepository cartRepository;
    private final UserManager userManager;
    private final ExecutorService executorService;

    public ProductRepository(ProductDao productDao, CartRepository cartRepository, UserManager userManager) {
        this.productDao = productDao;
        this.cartRepository = cartRepository;
        this.userManager = userManager;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllProducts();
    }

    public LiveData<Product> getProductById(int productId) {
        return productDao.getProductById(productId);
    }

    public Product getProduct(int productId) throws Exception {
        return productDao.getProductSync(productId);
    }

    public LiveData<List<Product>> getProductsByCategory(int categoryId) {
        return productDao.getProductsByCategory(categoryId);
    }

    public LiveData<List<Product>> searchProducts(String query) {
        return productDao.searchProducts(query);
    }

    public LiveData<List<Product>> getProductsByPriceRange(float minPrice, float maxPrice) {
        return productDao.getProductsByPriceRange(minPrice, maxPrice);
    }

    public LiveData<List<Product>> getTopRatedProducts(int limit) {
        return productDao.getTopRatedProducts(limit);
    }

    public LiveData<List<Product>> getFavoriteProducts() throws IllegalStateException {
        userManager.validateUserSession();
        return productDao.getFavoriteProducts(userManager.getCurrentUserId());
    }

    // Favorites management - TODO: Move to UserRepository
    public boolean isProductFavorite(int productId) throws Exception {
        throw new UnsupportedOperationException("Favorites management moved to UserRepository");
    }

    public void setProductFavorite(int productId, boolean isFavorite) throws Exception {
        throw new UnsupportedOperationException("Favorites management moved to UserRepository");
    }

    // Cart management - delegated to CartRepository
    public void addToCart(int productId, int quantity) throws Exception {
        android.util.Log.d("ProductRepository", "Delegating addToCart to CartRepository");
        cartRepository.addToCart(productId, quantity, new CartOperationCallback() {
            @Override
            public void onSuccess() {
                android.util.Log.d("ProductRepository", "Cart operation succeeded");
            }

            @Override
            public void onError(com.example.b_shop.data.local.errors.CartError error) {
                android.util.Log.e("ProductRepository", "Cart operation failed: " + error.getDetails());
            }
        });
    }

    public void updateProductRating(int productId) {
        executorService.execute(() -> {
            productDao.updateProductRating(productId);
        });
    }

    public void updateStock(int productId, int quantityChange) {
        executorService.execute(() -> {
            try {
                // If quantityChange is negative, we're decreasing stock
                // If quantityChange is positive, we're increasing stock
                // decreaseStock method decreases by the amount passed, so we negate positive quantityChange
                productDao.decreaseStock(productId, -quantityChange);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void cleanup() {
        executorService.shutdown();
    }
}