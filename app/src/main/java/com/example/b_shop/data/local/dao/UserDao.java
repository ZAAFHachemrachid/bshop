package com.example.b_shop.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.local.entities.Order;
import com.example.b_shop.data.local.entities.Review;
import com.example.b_shop.data.local.entities.Product;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Update
    void update(User user);

    @Delete
    void delete(User user);

    @Query("SELECT * FROM users WHERE userId = :userId")
    LiveData<User> getUserById(int userId);

    @Query("SELECT * FROM users WHERE userId = :userId")
    User getUserByIdSync(int userId);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    LiveData<User> getUserByEmail(String email);

    @Query("SELECT COUNT(*) FROM users WHERE email = :email")
    int checkEmailExists(String email);

    @Query("SELECT * FROM users WHERE email = :email AND password = :hashedPassword LIMIT 1")
    User authenticate(String email, String hashedPassword);

    // Get user's orders
    @Transaction
    @Query("SELECT * FROM orders WHERE userId = :userId ORDER BY orderDate DESC")
    LiveData<List<Order>> getUserOrders(int userId);

    // Get user's reviews
    @Transaction
    @Query("SELECT r.*, p.name as productName " +
           "FROM reviews r " +
           "INNER JOIN products p ON r.productId = p.productId " +
           "WHERE r.userId = :userId " +
           "ORDER BY r.createdAt DESC")
    LiveData<List<UserReviewWithProduct>> getUserReviews(int userId);

    // Get user's activity summary
    @Transaction
    @Query("SELECT u.*, " +
           "(SELECT COUNT(*) FROM orders WHERE userId = u.userId) as orderCount, " +
           "(SELECT COUNT(*) FROM reviews WHERE userId = u.userId) as reviewCount " +
           "FROM users u " +
           "WHERE u.userId = :userId")
    LiveData<UserActivity> getUserActivity(int userId);

    // Favorites management
    @Query("SELECT EXISTS(SELECT 1 FROM user_favorites WHERE userId = :userId AND productId = :productId)")
    boolean isProductFavorite(int userId, int productId);

    @Query("INSERT INTO user_favorites (userId, productId) VALUES (:userId, :productId)")
    void addToFavorites(int userId, int productId);

    @Query("DELETE FROM user_favorites WHERE userId = :userId AND productId = :productId")
    void removeFromFavorites(int userId, int productId);

    @Query("SELECT p.* FROM products p " +
           "INNER JOIN user_favorites uf ON p.productId = uf.productId " +
           "WHERE uf.userId = :userId")
    LiveData<List<Product>> getFavoriteProductsForUser(int userId);

    // Cart management - Deprecated: Use CartDao instead
    @Deprecated
    @Query("INSERT INTO cart_items (userId, productId, quantity) VALUES (:userId, :productId, :quantity)")
    default void addToCart(int userId, int productId, int quantity) {
        throw new UnsupportedOperationException("Cart operations moved to CartDao - use CartRepository instead");
    }

    @Deprecated
    @Query("DELETE FROM cart_items WHERE userId = :userId AND productId = :productId")
    default void removeFromCart(int userId, int productId) {
        throw new UnsupportedOperationException("Cart operations moved to CartDao - use CartRepository instead");
    }

    @Deprecated
    @Query("UPDATE cart_items SET quantity = :quantity WHERE userId = :userId AND productId = :productId")
    default void updateCartQuantity(int userId, int productId, int quantity) {
        throw new UnsupportedOperationException("Cart operations moved to CartDao - use CartRepository instead");
    }

    // User info retrieval
    @Query("SELECT name FROM users WHERE userId = :userId")
    String getUserNameSync(int userId);

    @Query("SELECT avatarUrl FROM users WHERE userId = :userId")
    String getUserAvatarUrlSync(int userId);

    // Static classes for complex queries
    static class UserReviewWithProduct {
        @Embedded
        public Review review;
        public String productName;
    }

    static class UserActivity {
        @Embedded
        public User user;
        public int orderCount;
        public int reviewCount;
    }
}