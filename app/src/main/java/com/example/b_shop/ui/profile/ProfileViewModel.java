package com.example.b_shop.ui.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.b_shop.data.local.dao.UserDao;
import com.example.b_shop.data.local.entities.Order;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

import java.util.List;

public class ProfileViewModel extends ViewModel {
    private final UserRepository userRepository;
    private final UserManager userManager;
    
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private LiveData<User> userProfile;
    private LiveData<List<Order>> userOrders;
    private LiveData<UserDao.UserActivity> userActivity;

    public ProfileViewModel(UserRepository userRepository, UserManager userManager) {
        this.userRepository = userRepository;
        this.userManager = userManager;
        loadUserProfile();
    }

    private void loadUserProfile() {
        int userId = userManager.getCurrentUserId();
        if (userId != -1) {
            isLoading.setValue(true);
            userProfile = userRepository.getUserById(userId);
            userOrders = userRepository.getUserOrders(userId);
            userActivity = userRepository.getUserActivity(userId);
            isLoading.setValue(false);
        } else {
            error.setValue("User not logged in");
        }
    }

    public void updateProfile(String name, String email, String phone) {
        isLoading.setValue(true);
        try {
            User currentUser = userProfile.getValue();
            if (currentUser != null) {
                currentUser.setName(name);
                currentUser.setEmail(email);
                currentUser.setPhone(phone);
                
                userRepository.updateProfile(currentUser);
                userManager.updateUserProfile(name, email);
                error.setValue(null);
            }
        } catch (Exception e) {
            error.setValue("Failed to update profile: " + e.getMessage());
        } finally {
            isLoading.setValue(false);
        }
    }

    public void logout() {
        userManager.logoutUser();
        userRepository.logout();
    }

    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public LiveData<List<Order>> getUserOrders() {
        return userOrders;
    }

    public LiveData<UserDao.UserActivity> getUserActivity() {
        return userActivity;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public void clearError() {
        error.setValue(null);
    }
}