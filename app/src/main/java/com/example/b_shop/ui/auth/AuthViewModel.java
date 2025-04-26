package com.example.b_shop.ui.auth;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.b_shop.R;
import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    
    private final UserRepository userRepository;
    private final UserManager userManager;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();
    private final SharedPreferences securePreferences;
    private final Executor backgroundExecutor;

    private static final String SECURE_PREFS_FILE = "secure_credentials";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";
    private static final String ENCRYPTION_KEY = "BShop_SecureStorage_Key";

    public AuthViewModel(@NonNull Application application,
                       @NonNull UserRepository userRepository,
                       @NonNull UserManager userManager) {
        super(application);
        this.userRepository = userRepository;
        this.userManager = userManager;
        this.securePreferences = application.getSharedPreferences(SECURE_PREFS_FILE, Context.MODE_PRIVATE);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.NO_WRAP);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }

    private String encryptData(String data) {
        try {
            byte[] input = data.getBytes(StandardCharsets.UTF_8);
            byte[] keyBytes = ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
            
            // Simple XOR encryption with the key
            byte[] encrypted = new byte[input.length];
            for (int i = 0; i < input.length; i++) {
                encrypted[i] = (byte) (input[i] ^ keyBytes[i % keyBytes.length]);
            }
            
            return Base64.encodeToString(encrypted, Base64.NO_WRAP);
        } catch (Exception e) {
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }

    private String decryptData(String encryptedData) {
        try {
            byte[] encrypted = Base64.decode(encryptedData, Base64.NO_WRAP);
            byte[] keyBytes = ENCRYPTION_KEY.getBytes(StandardCharsets.UTF_8);
            
            // Simple XOR decryption with the key
            byte[] decrypted = new byte[encrypted.length];
            for (int i = 0; i < encrypted.length; i++) {
                decrypted[i] = (byte) (encrypted[i] ^ keyBytes[i % keyBytes.length]);
            }
            
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }

    public void login(String email, String password, boolean rememberMe) {
        boolean isAutoLogin = !email.isEmpty() && !password.isEmpty() && rememberMe;
        backgroundExecutor.execute(() -> {
            try {
                String hashedPassword = hashPassword(password);
                User user = userRepository.login(email, hashedPassword).get();
                if (user != null) {
                    // Update session in UserManager
                    userManager.loginUser(user.getUserId(), user.getEmail(), user.getName());
                    
                    if (rememberMe) {
                        saveUserCredentials(email, password);
                    }
                    loginResult.postValue(new LoginResult(true, null, isAutoLogin));
                } else {
                    loginResult.postValue(new LoginResult(false, "Invalid email or password", isAutoLogin));
                }
            } catch (Exception e) {
                loginResult.postValue(new LoginResult(false, "Login attempt failed", isAutoLogin));
            }
        });
    }

    public void register(String email, String name, String phone, String password) {
        backgroundExecutor.execute(() -> {
            try {
                String hashedPassword = hashPassword(password);
                boolean success = userRepository.register(email, name, phone, hashedPassword).get();
                if (success) {
                    registrationResult.postValue(new RegistrationResult(true, null));
                } else {
                    registrationResult.postValue(new RegistrationResult(false, "Email already exists"));
                }
            } catch (Exception e) {
                registrationResult.postValue(new RegistrationResult(false, "Registration failed: " + e.getMessage()));
            }
        });
    }

    private void saveUserCredentials(String email, String password) {
        String encryptedEmail = encryptData(email);
        String encryptedPassword = encryptData(password);
        
        securePreferences.edit()
                .putString(KEY_SAVED_EMAIL, encryptedEmail)
                .putString(KEY_SAVED_PASSWORD, encryptedPassword)
                .apply();
    }

    public String getSavedEmail() {
        String encryptedEmail = securePreferences.getString(KEY_SAVED_EMAIL, "");
        return !encryptedEmail.isEmpty() ? decryptData(encryptedEmail) : "";
    }

    public String getSavedPassword() {
        String encryptedPassword = securePreferences.getString(KEY_SAVED_PASSWORD, "");
        return !encryptedPassword.isEmpty() ? decryptData(encryptedPassword) : "";
    }

    public void clearSavedCredentials() {
        securePreferences.edit()
                .remove(KEY_SAVED_EMAIL)
                .remove(KEY_SAVED_PASSWORD)
                .apply();
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<RegistrationResult> getRegistrationResult() {
        return registrationResult;
    }

    public void logout() {
        userManager.logoutUser();
        userRepository.logout();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        userRepository.cleanup();
    }

    public static class LoginResult {
        private final boolean success;
        private final String error;
        private final boolean isAutoLogin;

        public LoginResult(boolean success, String error) {
            this(success, error, false);
        }

        public LoginResult(boolean success, String error, boolean isAutoLogin) {
            this.success = success;
            this.error = error;
            this.isAutoLogin = isAutoLogin;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }

        public boolean isAutoLogin() {
            return isAutoLogin;
        }
    }

    public static class RegistrationResult {
        private final boolean success;
        private final String error;

        public RegistrationResult(boolean success, String error) {
            this.success = success;
            this.error = error;
        }

        public boolean isSuccess() {
            return success;
        }

        public String getError() {
            return error;
        }
    }
}