package com.example.b_shop.ui.auth;

import android.app.Application;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import com.example.b_shop.data.local.AppDatabase;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.example.b_shop.data.local.entities.User;
import com.example.b_shop.data.repositories.UserRepository;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class AuthViewModel extends AndroidViewModel {
    
    private final UserRepository userRepository;
    private final SharedPreferences preferences;
    private final MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private final MutableLiveData<RegistrationResult> registrationResult = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        AppDatabase database = AppDatabase.getInstance(application);
        userRepository = new UserRepository(database.userDao());
        preferences = PreferenceManager.getDefaultSharedPreferences(application);
    }

    public void login(String email, String password, boolean rememberMe) {
        try {
            Future<User> future = userRepository.login(email, password);
            User user = future.get();

            if (user != null) {
                if (rememberMe) {
                    saveUserCredentials(email, password);
                }
                loginResult.setValue(new LoginResult(true, null));
            } else {
                loginResult.setValue(new LoginResult(false, "Invalid email or password"));
            }
        } catch (ExecutionException | InterruptedException e) {
            loginResult.setValue(new LoginResult(false, "Login failed: " + e.getMessage()));
        }
    }

    public void register(String email, String name, String phone, String password) {
        try {
            Future<Boolean> future = userRepository.register(email, name, phone, password);
            boolean success = future.get();

            if (success) {
                registrationResult.setValue(new RegistrationResult(true, null));
            } else {
                registrationResult.setValue(new RegistrationResult(false, "Email already exists"));
            }
        } catch (ExecutionException | InterruptedException e) {
            registrationResult.setValue(new RegistrationResult(false, "Registration failed: " + e.getMessage()));
        }
    }

    private void saveUserCredentials(String email, String password) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("saved_email", email);
        editor.putString("saved_password", password);
        editor.apply();
    }

    public String getSavedEmail() {
        return preferences.getString("saved_email", "");
    }

    public String getSavedPassword() {
        return preferences.getString("saved_password", "");
    }

    public void clearSavedCredentials() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("saved_email");
        editor.remove("saved_password");
        editor.apply();
    }

    public LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public LiveData<RegistrationResult> getRegistrationResult() {
        return registrationResult;
    }

    public void logout() {
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