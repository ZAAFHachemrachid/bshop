package com.example.b_shop.ui.auth;

import android.app.Application;
import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthViewModel extends AndroidViewModel {
    
    private final UserRepository userRepository;
    private final UserManager userManager;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();
    private final EncryptedSharedPreferences securePreferences;
    private final Executor backgroundExecutor;

    private static final String SECURE_PREFS_FILE = "secure_credentials";
    private static final String KEY_SAVED_EMAIL = "saved_email";
    private static final String KEY_SAVED_PASSWORD = "saved_password";

    public AuthViewModel(@NonNull Application application,
                       @NonNull UserRepository userRepository,
                       @NonNull UserManager userManager) {
        super(application);
        this.userRepository = userRepository;
        this.userManager = userManager;
        this.securePreferences = createSecurePreferences(application);
        this.backgroundExecutor = Executors.newSingleThreadExecutor();
    }

    private EncryptedSharedPreferences createSecurePreferences(Context context) {
        try {
            KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(
                    "_auth_key_",
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build();

            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyGenParameterSpec(spec)
                    .build();

            return (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    context,
                    SECURE_PREFS_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize secure storage", e);
        }
    }

    public void login(String email, String password, boolean rememberMe) {
        backgroundExecutor.execute(() -> {
            try {
                User user = userRepository.login(email, password).get();
                if (user != null) {
                    // Update session in UserManager
                    userManager.loginUser(user.getId(), user.getEmail(), user.getName());
                    
                    if (rememberMe) {
                        saveUserCredentials(email, password);
                    }
                    loginResult.postValue(new LoginResult(true, null));
                } else {
                    loginResult.postValue(new LoginResult(false, "Invalid email or password"));
                }
            } catch (Exception e) {
                loginResult.postValue(new LoginResult(false, "Login failed: " + e.getMessage()));
            }
        });
    }

    public void register(String email, String name, String phone, String password) {
        backgroundExecutor.execute(() -> {
            try {
                boolean success = userRepository.register(email, name, phone, password).get();
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
        securePreferences.edit()
                .putString(KEY_SAVED_EMAIL, email)
                .putString(KEY_SAVED_PASSWORD, password)
                .apply();
    }

    public String getSavedEmail() {
        return securePreferences.getString(KEY_SAVED_EMAIL, "");
    }

    public String getSavedPassword() {
        return securePreferences.getString(KEY_SAVED_PASSWORD, "");
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

        public LoginResult(boolean success, String error) {
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