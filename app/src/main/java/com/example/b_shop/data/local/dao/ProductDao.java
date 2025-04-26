package com.example.b_shop.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.b_shop.data.local.entities.Product;
import java.util.List;

@Dao
public interface ProductDao {
    @Insert
    long insert(Product product);

    @Update
    void update(Product product);

    @Delete
    void delete(Product product);

    @Query("SELECT * FROM products")
    LiveData<List<Product>> getAllProducts();

    @Query("SELECT * FROM products WHERE productId = :productId")
    LiveData<Product> getProductById(int productId);

    // New synchronous method
    @Query("SELECT * FROM products WHERE productId = :productId")
    Product getProductSync(int productId);

    @Query("SELECT * FROM products WHERE categoryId = :categoryId")
    LiveData<List<Product>> getProductsByCategory(int categoryId);

    @Query("SELECT * FROM products WHERE " +
           "name LIKE '%' || :query || '%' OR " +
           "description LIKE '%' || :query || '%'")
    LiveData<List<Product>> searchProducts(String query);

    @Query("SELECT * FROM products WHERE price BETWEEN :minPrice AND :maxPrice")
    LiveData<List<Product>> getProductsByPriceRange(float minPrice, float maxPrice);

    @Query("SELECT * FROM products WHERE stock > 0")
    LiveData<List<Product>> getAvailableProducts();

    @Query("SELECT * FROM products ORDER BY rating DESC LIMIT :limit")
    LiveData<List<Product>> getTopRatedProducts(int limit);

    @Query("SELECT p.* FROM products p " +
           "INNER JOIN user_favorites f ON p.productId = f.productId " +
           "WHERE f.userId = :userId")
    LiveData<List<Product>> getFavoriteProducts(int userId);

    @Query("UPDATE products SET stock = stock - :quantity WHERE productId = :productId")
    void decreaseStock(int productId, int quantity);

    @Query("UPDATE products " +
           "SET rating = (SELECT AVG(CAST(rating AS FLOAT)) FROM reviews WHERE productId = :productId) " +
           "WHERE productId = :productId")
    void updateProductRating(int productId);

    // New method to update review count
    @Query("UPDATE products " +
           "SET reviewCount = (SELECT COUNT(*) FROM reviews WHERE productId = :productId) " +
           "WHERE productId = :productId")
    void updateReviewCount(int productId);

    // Transaction to update both rating and review count
    @Transaction
    default void updateProductReviewStats(int productId) {
        updateProductRating(productId);
        updateReviewCount(productId);
    }

    @Transaction
    @Query("SELECT p.*, c.name as categoryName " +
           "FROM products p " +
           "INNER JOIN categories c ON p.categoryId = c.categoryId " +
           "WHERE p.productId = :productId")
    LiveData<ProductWithCategory> getProductWithCategory(int productId);

    static class ProductWithCategory {
        @Embedded
        public Product product;
        public String categoryName;
    }
}