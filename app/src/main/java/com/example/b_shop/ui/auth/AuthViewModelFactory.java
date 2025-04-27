package com.example.b_shop.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.b_shop.data.local.AppDatabase;
import com.example.b_shop.data.repositories.UserRepository;

/**
 * Factory for creating AuthViewModel instances with required dependencies.
 */
public class AuthViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final UserRepository userRepository;

    private AuthViewModelFactory(@NonNull Application application) {
        this.application = application;
        AppDatabase database = AppDatabase.getInstance(application);
        this.userRepository = new UserRepository(database.userDao());
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(application, userRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }

    /**
     * Singleton instance
     */
    private static volatile AuthViewModelFactory instance;

    /**
     * Gets the singleton instance of AuthViewModelFactory
     * 
     * @param application The application context
     * @return The singleton instance of AuthViewModelFactory
     */
    public static AuthViewModelFactory getInstance(@NonNull Application application) {
        if (instance == null) {
            synchronized (AuthViewModelFactory.class) {
                if (instance == null) {
                    instance = new AuthViewModelFactory(application);
                }
            }
        }
        return instance;
    }

    /**
     * Cleans up the singleton instance.
     * Should be called when the app is being destroyed.
     */
    public static void cleanup() {
        instance = null;
    }
}