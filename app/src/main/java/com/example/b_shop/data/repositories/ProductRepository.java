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

    public ProductRepository(Application application) {
        AppDatabase database = AppDatabase.getInstance(application);
        productDao = database.productDao();
        userDao = database.userDao();
        executorService = Executors.newSingleThreadExecutor();
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

    public LiveData<ProductDao.ProductWithCategory> getProductWithCategory(int productId) {
        return productDao.getProductWithCategory(productId);
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

    // New methods for favorites
    public boolean isProductFavorite(int productId) throws Exception {
        return userDao.isProductFavorite(productId);
    }

    public void setProductFavorite(int productId, boolean isFavorite) throws Exception {
        if (isFavorite) {
            userDao.addToFavorites(productId);
        } else {
            userDao.removeFromFavorites(productId);
        }
    }

    // New methods for cart
    public void addToCart(int productId, int quantity) throws Exception {
        // Check stock first
        Product product = getProduct(productId);
        if (product.getStock() >= quantity) {
            userDao.addToCart(productId, quantity);
        } else {
            throw new Exception("Not enough stock available");
        }
    }

    public void removeFromCart(int productId) throws Exception {
        userDao.removeFromCart(productId);
    }

    public void updateCartQuantity(int productId, int quantity) throws Exception {
        Product product = getProduct(productId);
        if (product.getStock() >= quantity) {
            userDao.updateCartQuantity(productId, quantity);
        } else {
            throw new Exception("Not enough stock available");
        }
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