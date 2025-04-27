package com.example.b_shop.ui.profile;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

public class ProfileViewModelFactory implements ViewModelProvider.Factory {
    private final UserRepository userRepository;
    private final UserManager userManager;
    private final BShopApplication application;

    private static volatile ProfileViewModelFactory instance;

    private ProfileViewModelFactory(Context context, UserManager userManager) {
        android.util.Log.d("ProfileViewModelFactory", "Initializing with context and UserManager");
        
        this.application = (BShopApplication) context.getApplicationContext();
        this.userManager = userManager;
        this.userRepository = UserRepository.getInstance(context);
    }

    public static ProfileViewModelFactory getInstance(Context context, UserManager userManager) {
        if (instance == null) {
            synchronized (ProfileViewModelFactory.class) {
                if (instance == null) {
                    instance = new ProfileViewModelFactory(context, userManager);
                }
            }
        }
        return instance;
    }

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(ProfileViewModel.class)) {
            return (T) new ProfileViewModel(application, userRepository, userManager);
        }
        throw new IllegalArgumentException("Unknown ViewModel class: " + modelClass.getName());
    }
}