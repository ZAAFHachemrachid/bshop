package com.example.b_shop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavOptions;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.R;
import com.example.b_shop.databinding.ActivityAuthBinding;
import com.example.b_shop.ui.MainActivity;
import com.google.android.material.snackbar.Snackbar;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AuthActivity extends AppCompatActivity {

    private ActivityAuthBinding binding;
    private AuthViewModel viewModel;
    private NavController navController;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private Bundle savedNavState;
    private NavController.OnDestinationChangedListener destinationChangedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupNavigation(savedInstanceState);
        setupViewModel();
        observeAuthState();
        handleDeepLink();
        
        if (savedInstanceState == null) {
            checkSavedCredentials();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (navController != null) {
            outState.putBundle("nav_state", navController.saveState());
        }
    }

    private void setupNavigation(@NonNull Bundle savedInstanceState) {
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // Restore navigation state if available
            if (savedInstanceState != null) {
                Bundle navState = savedInstanceState.getBundle("nav_state");
                if (navState != null) {
                    try {
                        navController.restoreState(navState);
                    } catch (IllegalStateException e) {
                        // Handle restoration failure gracefully
                        navController.navigate(R.id.loginFragment);
                    }
                }
            }

            // Save state before configuration changes
            getSupportFragmentManager().setFragmentResultListener(
                "config_change", this,
                (requestKey, result) -> {
                    savedNavState = navController.saveState();
                }
            );
            
            AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.loginFragment, R.id.registerFragment
            ).build();
            
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
            
            // Initialize and store destination changed listener
            destinationChangedListener = (controller, destination, arguments) -> {
                if (destination.getId() == R.id.mainActivity) {
                    // Use proper navigation action with popUpTo flags instead of clearing backstack
                    NavOptions navOptions = new NavOptions.Builder()
                        .setPopUpTo(R.id.nav_graph_auth, true)
                        .build();
                    controller.navigate(R.id.mainActivity, null, navOptions);
                }
            };
            navController.addOnDestinationChangedListener(destinationChangedListener);
        }
    }

    private void handleDeepLink() {
        Intent intent = getIntent();
        if (intent != null && intent.getData() != null) {
            try {
                navController.handleDeepLink(intent);
            } catch (Exception e) {
                Snackbar.make(binding.getRoot(),
                    getString(R.string.error_invalid_deep_link),
                    Snackbar.LENGTH_LONG).show();
                // Navigate to default destination on deep link failure
                navController.navigate(R.id.loginFragment);
            }
        }
    }

    private void setupViewModel() {
        AuthViewModelFactory factory = new AuthViewModelFactory(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
    }

    private void observeAuthState() {
        viewModel.getLoginResult().observe(this, result -> {
            binding.progressBar.setVisibility(View.GONE);
            
            if (result.isSuccess()) {
                // Navigate to MainActivity on successful login
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                // Show error message
                String errorMessage = result.getError() != null ?
                    result.getError() :
                    getString(R.string.error_login_failed);
                Snackbar.make(binding.getRoot(), errorMessage, Snackbar.LENGTH_LONG).show();
                
                // Clear saved credentials if auto-login failed
                if (result.isAutoLogin()) {
                    viewModel.clearSavedCredentials();
                }
            }
        });
    }

    private void checkSavedCredentials() {
        String savedEmail = viewModel.getSavedEmail();
        String savedPassword = viewModel.getSavedPassword();

        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            binding.progressBar.setVisibility(View.VISIBLE);
            // Pass true as last parameter to indicate auto-login
            viewModel.login(savedEmail, savedPassword, true);
            // Login result will be handled in observeAuthState()
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewModel != null) {
            // Remove observers to prevent memory leaks
            viewModel.getLoginResult().removeObservers(this);
        }
        
        // Clean up navigation controller and listener
        if (navController != null) {
            if (destinationChangedListener != null) {
                navController.removeOnDestinationChangedListener(destinationChangedListener);
                destinationChangedListener = null;
            }
            navController.enableOnBackPressed(false);
            navController = null;
        }
        
        // Save state before destruction if not configuration change
        if (!isChangingConfigurations()) {
            savedNavState = null;
        }
        
        binding = null;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (navController != null) {
            navController.handleDeepLink(intent);
        }
    }
}