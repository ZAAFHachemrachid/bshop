package com.example.b_shop.data.repositories;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.dao.ProductDao;
import com.example.b_shop.data.local.dao.UserDao;
import com.example.b_shop.data.local.entities.Product;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProductRepository {
    private final ProductDao productDao;
    private final UserDao userDao;
    private final ExecutorService executorService;
    private final int currentUserId; // TODO: Get from UserManager/Session

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        productDao = database.productDao();
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
        currentUserId = 1; // TODO: Get from UserManager/Session
    }

    public void refreshFeaturedProducts() {
        executorService.execute(() -> {
            productDao.getTopRatedProducts(5);
        });
    }

    public void refreshTopRatedProducts() {
        executorService.execute(() -> {
            productDao.getTopRatedProducts(10);
        });
    }

    // Basic CRUD operations
    public void insert(Product product) {
        executorService.execute(() -> {
            productDao.insert(product);
        });
    }

    public void update(Product product) {
        executorService.execute(() -> {
            productDao.update(product);
        });
    }

    public void delete(Product product) {
        executorService.execute(() -> {
            productDao.delete(product);
        });
    }

    // Query methods
    public LiveData<List<Product>> getAllProducts() {
        return productDao.getAllProducts();
    }

    public LiveData<Product> getProductById(int productId) {
        return productDao.getProductById(productId);
    }

    // New method: Get product synchronously
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

    public LiveData<List<Product>> getAvailableProducts() {
        return productDao.getAvailableProducts();
    }

    public LiveData<List<Product>> getTopRatedProducts(int limit) {
        return productDao.getTopRatedProducts(limit);
    }

    // Favorites management
    public boolean isProductFavorite(int productId) throws Exception {
        return userDao.isProductFavorite(currentUserId, productId);
    }

    public void setProductFavorite(int productId, boolean isFavorite) throws Exception {
        if (isFavorite) {
            userDao.addToFavorites(currentUserId, productId);
        } else {
            userDao.removeFromFavorites(currentUserId, productId);
        }
    }

    // Cart management
    public void addToCart(int productId, int quantity) throws Exception {
        userDao.addToCart(currentUserId, productId, quantity);
    }

    public void removeFromCart(int productId) throws Exception {
        userDao.removeFromCart(currentUserId, productId);
    }

    public void updateCartQuantity(int productId, int quantity) throws Exception {
        userDao.updateCartQuantity(currentUserId, productId, quantity);
    }

    // Stock management
    public void decreaseStock(int productId, int quantity) {
        executorService.execute(() -> {
            productDao.decreaseStock(productId, quantity);
        });
    }

    // Rating management
    public void updateProductRating(int productId) {
        executorService.execute(() -> {
            productDao.updateProductRating(productId);
        });
    }

    // Cleanup
    public void cleanup() {
        executorService.shutdown();
    }

    // Helper method for filtering and sorting
    public LiveData<List<Product>> getFilteredAndSortedProducts(
            Integer categoryId,
            Float minPrice,
            Float maxPrice,
            Boolean inStockOnly,
            String sortBy) {
        
        if (categoryId != null) {
            return productDao.getProductsByCategory(categoryId);
        }
        
        if (minPrice != null && maxPrice != null) {
            return productDao.getProductsByPriceRange(minPrice, maxPrice);
        }
        
        if (inStockOnly != null && inStockOnly) {
            return productDao.getAvailableProducts();
        }

        if ("rating".equals(sortBy)) {
            return productDao.getTopRatedProducts(Integer.MAX_VALUE);
        }

        return productDao.getAllProducts();
    }
}