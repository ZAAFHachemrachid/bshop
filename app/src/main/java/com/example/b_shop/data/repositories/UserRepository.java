package com.example.b_shop.data.repositories;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.local.entities.Product;
import com.example.b_shop.data.local.dao.UserDao;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.local.entities.Order;
import com.example.b_shop.data.local.entities.UserRole;
import com.example.b_shop.data.local.entities.UserAuditLog;
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

    private static volatile UserRepository instance;

    public static UserRepository getInstance(Context context) {
        if (instance == null) {
            synchronized (UserRepository.class) {
                if (instance == null) {
                    AppDatabase database = AppDatabase.getInstance(context);
                    instance = new UserRepository(database.userDao());
                }
            }
        }
        return instance;
    }

    public UserRepository(UserDao userDao) {
        this.userDao = userDao;
        this.executorService = Executors.newSingleThreadExecutor();
    }

    // Admin operations
    private void requireAdminUser() {
        if (currentUser == null || !currentUser.isAdmin()) {
            throw new SecurityException("Admin privileges required for this operation");
        }
    }

    public Future<Boolean> createAdminUser(String email, String name, String password) {
        requireAdminUser();
        return executorService.submit(() -> {
            if (userDao.checkEmailExists(email) > 0) {
                return false;
            }

            String hashedPassword = hashPassword(password);
            User newAdmin = new User(name, email, hashedPassword, UserRole.ADMIN);
            long userId = userDao.insert(newAdmin);

            if (userId > 0) {
                logAdminAction(getCurrentUser().getUserId(), (int) userId, 
                    UserAuditLog.Actions.USER_CREATED, "Created admin user: " + email);
                return true;
            }
            return false;
        });
    }

    public void updateUserRole(int userId, UserRole newRole) {
        requireAdminUser();
        executorService.execute(() -> {
            userDao.updateUserRole(userId, newRole);
            logAdminAction(getCurrentUser().getUserId(), userId,
                UserAuditLog.Actions.ROLE_CHANGED, 
                "Changed user role to: " + newRole);
        });
    }

    public void blockUser(int userId) {
        requireAdminUser();
        executorService.execute(() -> {
            userDao.updateUserActiveStatus(userId, false);
            logAdminAction(getCurrentUser().getUserId(), userId,
                UserAuditLog.Actions.USER_BLOCKED,
                "User blocked");
        });
    }

    public void unblockUser(int userId) {
        requireAdminUser();
        executorService.execute(() -> {
            userDao.updateUserActiveStatus(userId, true);
            logAdminAction(getCurrentUser().getUserId(), userId,
                UserAuditLog.Actions.USER_UNBLOCKED,
                "User unblocked");
        });
    }

    public LiveData<List<User>> getUsersByRole(UserRole role) {
        requireAdminUser();
        return userDao.getUsersByRole(role);
    }

    public LiveData<List<User>> getActiveUsers(UserRole role, long since) {
        requireAdminUser();
        return userDao.getActiveUsersByRole(role, since);
    }

    public LiveData<List<UserAuditLog>> getUserAuditLogs(int userId) {
        requireAdminUser();
        return userDao.getUserAuditLogs(userId);
    }

    public LiveData<List<UserAuditLog>> getAdminAuditLogs(int adminId) {
        requireAdminUser();
        return userDao.getAdminAuditLogs(adminId);
    }

    public LiveData<List<UserAuditLog>> getRecentAuditLogs(int limit) {
        requireAdminUser();
        return userDao.getRecentAuditLogs(limit);
    }

    public void logAdminAction(int adminId, int userId, String action, String details) {
        UserAuditLog log = new UserAuditLog(userId, adminId, action, details);
        executorService.execute(() -> userDao.insertAuditLog(log));
    }

    // Authentication methods
    public Future<User> login(String email, String password) {
        return executorService.submit(() -> {
            String hashedPassword = hashPassword(password);
            User user = userDao.authenticate(email, hashedPassword);
            if (user != null) {
                currentUser = user;
                userDao.updateLastLogin(user.getUserId());
                user.updateLastLogin();
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
            newUser.setPhone(phone);
            
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

    // Favorites management
    public LiveData<List<Product>> getFavoriteProducts() {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to access favorites");
        }
        return userDao.getFavoriteProductsForUser(currentUser.getUserId());
    }

    public void addToFavorites(int productId) {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to manage favorites");
        }
        executorService.execute(() -> {
            userDao.addToFavorites(currentUser.getUserId(), productId);
        });
    }

    public void removeFromFavorites(int productId) {
        if (currentUser == null) {
            throw new IllegalStateException("User must be logged in to manage favorites");
        }
        executorService.execute(() -> {
            userDao.removeFromFavorites(currentUser.getUserId(), productId);
        });
    }

    public boolean isProductFavorite(int productId) {
        if (currentUser == null) {
            return false;
        }
        return userDao.isProductFavorite(currentUser.getUserId(), productId);
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
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    public Future<Boolean> changePassword(int userId, String oldPassword, String newPassword) {
        return executorService.submit(() -> {
            String hashedOldPassword = hashPassword(oldPassword);
            User user = userDao.getUserByIdSync(userId);
            
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