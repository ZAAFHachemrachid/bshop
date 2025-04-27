package com.example.b_shop.ui.profile;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.local.entities.UserRole;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ProfileViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final UserManager userManager;
    private final ExecutorService executorService;
    private final MutableLiveData<Boolean> isAdminUser;
    private final MutableLiveData<String> error;

    public ProfileViewModel(@NonNull Application application,
                          @NonNull UserRepository userRepository,
                          @NonNull UserManager userManager) {
        super(application);
        this.userRepository = userRepository;
        this.userManager = userManager;
        this.executorService = Executors.newSingleThreadExecutor();
        this.isAdminUser = new MutableLiveData<>(false);
        this.error = new MutableLiveData<>();

        // Check if current user is admin
        checkAdminStatus();
    }

    private void checkAdminStatus() {
        if (userManager.isUserLoggedIn()) {
            User currentUser = userRepository.getCurrentUser();
            if (currentUser != null) {
                isAdminUser.setValue(currentUser.getRole() == UserRole.ADMIN);
            }
        }
    }

    public void logout() {
        userManager.logoutUser();
        userRepository.logout();
        isAdminUser.setValue(false);
    }

    public boolean isLoggedIn() {
        return userManager.isUserLoggedIn();
    }

    public LiveData<Boolean> getIsAdminUser() {
        return isAdminUser;
    }

    public LiveData<String> getError() {
        return error;
    }

    public String getCurrentUserName() {
        return userManager.getCurrentUserName();
    }

    public String getCurrentUserEmail() {
        return userManager.getCurrentUserEmail();
    }

    /**
     * Attempts to access admin dashboard
     * @return true if access is granted, false otherwise
     */
    public boolean canAccessAdminDashboard() {
        try {
            return userManager.isCurrentUserAdmin();
        } catch (SecurityException e) {
            error.setValue(e.getMessage());
            return false;
        }
    }

    /**
     * Validates the admin session before performing admin operations
     * @return true if session is valid, false otherwise
     */
    public boolean validateAdminSession() {
        try {
            return userManager.validateAdminSession();
        } catch (SecurityException e) {
            error.setValue(e.getMessage());
            return false;
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}