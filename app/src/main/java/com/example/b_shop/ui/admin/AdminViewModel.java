package com.example.b_shop.ui.admin;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.local.entities.UserRole;
import com.example.b_shop.data.local.entities.UserAuditLog;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;
import com.example.b_shop.utils.annotations.RequiresAdmin;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class AdminViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final UserManager userManager;
    private final ExecutorService executorService;
    private final MutableLiveData<AdminOperationResult> operationResult;

    public AdminViewModel(@NonNull Application application,
                        @NonNull UserRepository userRepository,
                        @NonNull UserManager userManager) {
        super(application);
        this.userRepository = userRepository;
        this.userManager = userManager;
        this.executorService = Executors.newSingleThreadExecutor();
        this.operationResult = new MutableLiveData<>();
    }

    @RequiresAdmin(
        description = "Create new admin user",
        audit = true
    )
    public void createAdminUser(String email, String name, String password) {
        executorService.execute(() -> {
            try {
                boolean success = userRepository.createAdminUser(email, name, password).get();
                if (success) {
                    operationResult.postValue(new AdminOperationResult(true, "Admin user created successfully"));
                } else {
                    operationResult.postValue(new AdminOperationResult(false, "Failed to create admin user"));
                }
            } catch (Exception e) {
                operationResult.postValue(new AdminOperationResult(false, "Error: " + e.getMessage()));
            }
        });
    }

    @RequiresAdmin(
        description = "Update user role",
        audit = true
    )
    public void updateUserRole(int userId, UserRole newRole) {
        executorService.execute(() -> {
            try {
                userRepository.updateUserRole(userId, newRole);
                operationResult.postValue(new AdminOperationResult(true, "User role updated successfully"));
            } catch (Exception e) {
                operationResult.postValue(new AdminOperationResult(false, "Error: " + e.getMessage()));
            }
        });
    }

    @RequiresAdmin(
        description = "Block user",
        audit = true
    )
    public void blockUser(int userId) {
        executorService.execute(() -> {
            try {
                userRepository.blockUser(userId);
                operationResult.postValue(new AdminOperationResult(true, "User blocked successfully"));
            } catch (Exception e) {
                operationResult.postValue(new AdminOperationResult(false, "Error: " + e.getMessage()));
            }
        });
    }

    @RequiresAdmin(
        description = "Unblock user",
        audit = true
    )
    public void unblockUser(int userId) {
        executorService.execute(() -> {
            try {
                userRepository.unblockUser(userId);
                operationResult.postValue(new AdminOperationResult(true, "User unblocked successfully"));
            } catch (Exception e) {
                operationResult.postValue(new AdminOperationResult(false, "Error: " + e.getMessage()));
            }
        });
    }

    @RequiresAdmin(
        description = "View user list",
        audit = false
    )
    public LiveData<List<User>> getUsersByRole(UserRole role) {
        return userRepository.getUsersByRole(role);
    }

    @RequiresAdmin(
        description = "View active users",
        audit = false
    )
    public LiveData<List<User>> getActiveUsers(UserRole role, int lastHours) {
        long since = LocalDateTime.now().minus(lastHours, ChronoUnit.HOURS)
            .atZone(java.time.ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli() / 1000L;
        return userRepository.getActiveUsers(role, since);
    }

    @RequiresAdmin(
        description = "View user audit logs",
        audit = false
    )
    public LiveData<List<UserAuditLog>> getUserAuditLogs(int userId) {
        return userRepository.getUserAuditLogs(userId);
    }

    @RequiresAdmin(
        description = "View admin audit logs",
        audit = false
    )
    public LiveData<List<UserAuditLog>> getAdminAuditLogs(int adminId) {
        return userRepository.getAdminAuditLogs(adminId);
    }

    @RequiresAdmin(
        description = "View recent audit logs",
        audit = false
    )
    public LiveData<List<UserAuditLog>> getRecentAuditLogs(int limit) {
        return userRepository.getRecentAuditLogs(limit);
    }

    public LiveData<AdminOperationResult> getOperationResult() {
        return operationResult;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }

    public static class AdminOperationResult {
        private final boolean success;
        private final String message;

        public AdminOperationResult(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }
}