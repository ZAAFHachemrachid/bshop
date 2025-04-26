package com.example.b_shop.data.repositories;

import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.dao.UserDao;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.local.entities.Order;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Future;

public class UserRepository {
    private final UserDao userDao;
    private final ExecutorService executorService;
    private User currentUser;

    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Authentication methods
    public Future<User> login(String email, String password) {
        return executorService.submit(() -> {
            String hashedPassword = hashPassword(password);
            User user = userDao.authenticate(email, hashedPassword);
            if (user != null) {
                currentUser = user;
            }
            return user;
        });
    }

    public Future<Boolean> register(String email, String name, String phone, String password) {
        return executorService.submit(() -> {
            if (userDao.checkEmailExists(email) > 0) {
                return false;
            }

            String hashedPassword = hashPassword(password);
            User newUser = new User(name, email, hashedPassword);
            newUser.setPhone(phone); // Set phone number after creation
            
            long userId = userDao.insert(newUser);
            return userId > 0;
        });
    }

    public void logout() {
        currentUser = null;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    // Profile management
    public void updateProfile(User user) {
        executorService.execute(() -> {
            userDao.update(user);
            if (currentUser != null && currentUser.getUserId() == user.getUserId()) {
                currentUser = user;
            }
        });
    }

    public LiveData<User> getUserById(int userId) {
        return userDao.getUserById(userId);
    }

    public LiveData<User> getUserByEmail(String email) {
        return userDao.getUserByEmail(email);
    }

    // User activity methods
    public LiveData<List<Order>> getUserOrders(int userId) {
        return userDao.getUserOrders(userId);
    }

    public LiveData<List<UserDao.UserReviewWithProduct>> getUserReviews(int userId) {
        return userDao.getUserReviews(userId);
    }

    public LiveData<UserDao.UserActivity> getUserActivity(int userId) {
        return userDao.getUserActivity(userId);
    }

    // Password management
    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return password; // Fallback to plain password in case of error
        }
    }

    public Future<Boolean> changePassword(int userId, String oldPassword, String newPassword) {
        return executorService.submit(() -> {
            String hashedOldPassword = hashPassword(oldPassword);
            User user = userDao.getUserById(userId).getValue();
            
            if (user != null && user.getPassword().equals(hashedOldPassword)) {
                user.setPassword(hashPassword(newPassword));
                userDao.update(user);
                return true;
            }
            return false;
        });
    }

    // Cleanup
    public void cleanup() {
        executorService.shutdown();
    }
}