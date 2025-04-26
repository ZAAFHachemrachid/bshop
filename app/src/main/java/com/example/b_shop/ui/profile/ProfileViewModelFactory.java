package com.example.b_shop.ui.profile;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

public class ProfileViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;
    private final UserManager userManager;

    public ProfileViewModelFactory(BShopApplication application) {
        android.util.Log.d("ProfileViewModelFactory", "Initializing with dependencies from BShopApplication");
        
        // Get singleton instances
        this.userRepository = application.getUserRepository();
        this.userManager = application.getUserManager();
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(userRepository, userManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}