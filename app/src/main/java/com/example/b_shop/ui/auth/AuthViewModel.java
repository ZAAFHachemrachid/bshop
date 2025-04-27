package com.example.b_shop.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    private final UserRepository userRepository;
    private final UserManager userManager;
    private final ExecutorService executorService;
    private final MutableLiveData<AuthState> authState;
    private final MutableLiveData<AuthState> registrationState;

    public AuthViewModel(@NonNull Application application, @NonNull UserRepository userRepository) {
        super(application);
        this.userRepository = userRepository;
        this.userManager = UserManager.getInstance(application);
        this.executorService = Executors.newSingleThreadExecutor();
        this.authState = new MutableLiveData<>(AuthState.initial());
        this.registrationState = new MutableLiveData<>(AuthState.initial());
    }

    public void login(String email, String password) {
        if (!validateInput(email, password)) {
            return;
        }

        authState.setValue(AuthState.loading());
        executorService.execute(() -> {
            try {
                User user = userRepository.login(email, password).get();
                if (user != null) {
                    // For admin users, perform additional validation
                    if (user.isAdmin() && !validateAdminCredentials(email, password)) {
                        authState.postValue(AuthState.error("Invalid admin credentials"));
                        return;
                    }
                    authState.postValue(AuthState.success(user.getRole()));
                } else {
                    authState.postValue(AuthState.error("Invalid email or password"));
                }
            } catch (Exception e) {
                authState.postValue(AuthState.error(e.getMessage()));
            }
        });
    }

    public void register(String email, String name, String phone, String password) {
        if (!validateRegistrationInput(email, name, phone, password)) {
            return;
        }

        authState.setValue(AuthState.loading());
        executorService.execute(() -> {
            try {
                boolean success = userRepository.register(email, name, phone, password).get();
                if (success) {
                    // After registration, perform login
                    login(email, password);
                } else {
                    authState.postValue(AuthState.error("Registration failed. Email might be taken."));
                }
            } catch (Exception e) {
                authState.postValue(AuthState.error(e.getMessage()));
            }
        });
    }

    private boolean validateInput(String email, String password) {
        if (email == null || email.trim().isEmpty()) {
            authState.setValue(AuthState.error("Email is required"));
            return false;
        }

        if (password == null || password.trim().isEmpty()) {
            authState.setValue(AuthState.error("Password is required"));
            return false;
        }

        return true;
    }

    private boolean validateRegistrationInput(String email, String name, String phone, String password) {
        if (!validateInput(email, password)) {
            return false;
        }

        if (name == null || name.trim().isEmpty()) {
            authState.setValue(AuthState.error("Name is required"));
            return false;
        }

        if (phone == null || phone.trim().isEmpty()) {
            authState.setValue(AuthState.error("Phone number is required"));
            return false;
        }

        // Password complexity check for registration
        if (!isPasswordComplex(password)) {
            authState.setValue(AuthState.error(
                "Password must be at least 8 characters long and contain uppercase, " +
                "lowercase, number, and special character"
            ));
            return false;
        }

        return true;
    }

    private boolean validateAdminCredentials(String email, String password) {
        // Additional validation for admin users
        // In production, implement stronger validation and 2FA
        return email.endsWith("@bshop.com") && isPasswordComplex(password);
    }

    private boolean isPasswordComplex(String password) {
        return password.length() >= 8 &&
               password.matches(".*[A-Z].*") &&   // At least one uppercase
               password.matches(".*[a-z].*") &&   // At least one lowercase
               password.matches(".*\\d.*") &&     // At least one digit
               password.matches(".*[!@#$%^&*].*"); // At least one special character
    }

    public void clearError() {
        authState.setValue(AuthState.initial());
    }

    public LiveData<AuthState> getAuthState() {
        return authState;
    }

    public LiveData<AuthState> getRegistrationResult() {
        return registrationState;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}