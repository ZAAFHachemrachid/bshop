package com.example.b_shop.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Embedded;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.local.entities.Order;
import com.example.b_shop.data.local.entities.Review;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.local.entities.UserRole;
import com.example.b_shop.data.local.entities.UserAuditLog;
import java.util.List;

@Dao
public interface UserDao {
    @Insert
    long insert(User user);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insertIfNotExists(User user);

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

    @Query("SELECT * FROM users WHERE email = :email AND password = :hashedPassword AND isActive = 1 LIMIT 1")
    User authenticate(String email, String hashedPassword);

    // Admin specific queries
    @Query("SELECT * FROM users WHERE role = :role")
    LiveData<List<User>> getUsersByRole(UserRole role);

    @Query("UPDATE users SET isActive = :isActive WHERE userId = :userId")
    void updateUserActiveStatus(int userId, boolean isActive);

    @Query("UPDATE users SET role = :role WHERE userId = :userId")
    void updateUserRole(int userId, UserRole role);

    @Query("UPDATE users SET lastLogin = strftime('%s', 'now') WHERE userId = :userId")
    void updateLastLogin(int userId);

    @Query("SELECT COUNT(*) FROM users WHERE role = :role")
    int getCountByRole(UserRole role);

    // Audit logging
    @Insert
    void insertAuditLog(UserAuditLog auditLog);

    @Query("SELECT * FROM user_audit_log WHERE userId = :userId ORDER BY timestamp DESC")
    LiveData<List<UserAuditLog>> getUserAuditLogs(int userId);

    @Query("SELECT * FROM user_audit_log WHERE adminId = :adminId ORDER BY timestamp DESC")
    LiveData<List<UserAuditLog>> getAdminAuditLogs(int adminId);

    @Query("SELECT * FROM user_audit_log ORDER BY timestamp DESC LIMIT :limit")
    LiveData<List<UserAuditLog>> getRecentAuditLogs(int limit);

    // User activity tracking
    @Transaction
    @Query("SELECT * FROM users WHERE role = :role AND lastLogin >= :since")
    LiveData<List<User>> getActiveUsersByRole(UserRole role, long since);

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
           "(SELECT COUNT(*) FROM reviews WHERE userId = u.userId) as reviewCount, " +
           "(SELECT COUNT(*) FROM user_audit_log WHERE adminId = u.userId) as adminActionCount " +
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
        public int adminActionCount;
    }
}