package com.example.b_shop.ui.auth;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

public class AuthViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final UserRepository userRepository;
    private final UserManager userManager;

    public AuthViewModelFactory(@NonNull Application application) {
        this.application = application;
        BShopApplication app = (BShopApplication) application;
        this.userRepository = app.getUserRepository();
        this.userManager = app.getUserManager();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(AuthViewModel.class)) {
            return (T) new AuthViewModel(application, userRepository, userManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}