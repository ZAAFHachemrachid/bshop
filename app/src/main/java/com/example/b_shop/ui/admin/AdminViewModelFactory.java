package com.example.b_shop.ui.admin;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

/**
 * Factory for creating AdminViewModel instances with required dependencies.
 */
public class AdminViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final UserRepository userRepository;
    private final UserManager userManager;

    public AdminViewModelFactory(@NonNull Application application,
                               @NonNull UserRepository userRepository,
                               @NonNull UserManager userManager) {
        this.application = application;
        this.userRepository = userRepository;
        this.userManager = userManager;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AdminViewModel.class)) {
            return (T) new AdminViewModel(application, userRepository, userManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }

    /**
     * Creates an instance of AdminViewModelFactory.
     * 
     * @param application The application context
     * @param userRepository Repository for user operations
     * @param userManager Manager for user session
     * @return A new instance of AdminViewModelFactory
     */
    public static AdminViewModelFactory getInstance(@NonNull Application application,
                                                  @NonNull UserRepository userRepository,
                                                  @NonNull UserManager userManager) {
        return new AdminViewModelFactory(application, userRepository, userManager);
    }
}