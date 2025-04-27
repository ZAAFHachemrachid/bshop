package com.example.b_shop.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import com.example.b_shop.R;
import com.example.b_shop.data.local.entities.UserRole;
import com.example.b_shop.databinding.ActivityAuthBinding;
import com.example.b_shop.ui.MainActivity;
import com.example.b_shop.utils.UserManager;
import com.google.android.material.snackbar.Snackbar;

public class AuthActivity extends AppCompatActivity {
    private ActivityAuthBinding binding;
    private AuthViewModel viewModel;
    private UserManager userManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAuthBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        userManager = UserManager.getInstance(this);
        setupViewModel();
        observeAuthState();

        // If already logged in, redirect appropriately
        if (userManager.isUserLoggedIn()) {
            redirectBasedOnRole();
        }
    }

    private void setupViewModel() {
        AuthViewModelFactory factory = AuthViewModelFactory.getInstance(getApplication());
        viewModel = new ViewModelProvider(this, factory).get(AuthViewModel.class);
    }

    private void observeAuthState() {
        viewModel.getAuthState().observe(this, state -> {
            updateLoadingState(state.isLoading());

            if (state.isSuccess()) {
                handleAuthSuccess(state.getRole());
            }

            if (state.getError() != null) {
                handleAuthError(state.getError());
            }
        });
    }

    private void handleAuthSuccess(UserRole role) {
        String message = role == UserRole.ADMIN ?
            "Logged in as administrator" : "Login successful";
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        
        // For admin users, validate session immediately
        if (role == UserRole.ADMIN && !userManager.validateAdminSession()) {
            handleAuthError("Admin session validation failed");
            return;
        }

        redirectBasedOnRole();
    }

    private void handleAuthError(String error) {
        Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
            .setAction("Retry", v -> {
                // Clear error state
                viewModel.clearError();
            })
            .show();
    }

    private void redirectBasedOnRole() {
        Intent intent = new Intent(this, MainActivity.class);
        
        // If user is admin and was trying to access admin features
        if (userManager.isCurrentUserAdmin() && getIntent().getBooleanExtra("admin_required", false)) {
            intent.putExtra("open_admin_dashboard", true);
        }

        // Clear back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void updateLoadingState(boolean isLoading) {
        int visibility = isLoading ? View.VISIBLE : View.GONE;
        binding.progressBar.setVisibility(visibility);
        
        // Disable interaction while loading
        binding.getRoot().setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

    @Override
    public void onBackPressed() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        if (!navController.navigateUp()) {
            super.onBackPressed();
        }
    }
}