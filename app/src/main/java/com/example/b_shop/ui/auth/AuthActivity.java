package com.example.b_shop.ui.auth;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.example.b_shop.R;
import com.example.b_shop.databinding.ActivityAuthBinding;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private AuthViewModel viewModel;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation();
        setupViewModel();
        checkSavedCredentials();
    }

    private void setupNavigation() {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.loginFragment, R.id.registerFragment
            ).build();
            
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }

    private void setupViewModel() {
        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);
    }

    private void checkSavedCredentials() {
        String savedEmail = viewModel.getSavedEmail();
        String savedPassword = viewModel.getSavedPassword();

        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            viewModel.login(savedEmail, savedPassword, true);
            
            viewModel.getLoginResult().observe(this, result -> {
                if (!result.isSuccess()) {
                    // Clear invalid credentials
                    viewModel.clearSavedCredentials();
                }
            });
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}