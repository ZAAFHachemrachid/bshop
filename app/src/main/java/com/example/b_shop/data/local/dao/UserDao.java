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