package com.example.b_shop.ui.auth;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.example.b_shop.R;
import com.example.b_shop.databinding.FragmentLoginBinding;
import com.google.android.material.snackbar.Snackbar;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;
    private boolean isAdminLoginAttempt = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupViewModel();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        observeAuthState();
    }

    private void setupViewModel() {
        AuthViewModelFactory factory = AuthViewModelFactory.getInstance(requireActivity().getApplication());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(AuthViewModel.class);
    }

    private void setupUI() {
        // Register navigation
        binding.btnRegister.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_login_to_register));

        // Admin mode toggle
        binding.switchAdminMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            isAdminLoginAttempt = isChecked;
            updateUIForAdminMode(isChecked);
        });

        // Login button
        binding.btnLogin.setOnClickListener(v -> attemptLogin());

        // Make "Forgot Password" text clickable
        binding.tvForgotPassword.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvForgotPassword.setOnClickListener(v -> handleForgotPassword());

        // Check if admin login is required
        boolean adminRequired = requireActivity().getIntent()
            .getBooleanExtra("admin_required", false);
        if (adminRequired) {
            binding.switchAdminMode.setChecked(true);
            isAdminLoginAttempt = true;
            updateUIForAdminMode(true);
            showMessage("Admin authentication required");
        }
    }

    private void observeAuthState() {
        viewModel.getAuthState().observe(getViewLifecycleOwner(), state -> {
            updateLoadingState(state.isLoading());

            if (state.getError() != null) {
                showError(state.getError());
            }

            if (state.isSuccess()) {
                if (state.isAdmin()) {
                    showMessage("Admin login successful");
                }
                // Navigation handled by AuthActivity
            }
        });
    }

    private void attemptLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields");
            return;
        }

        // For admin login, enforce email domain
        if (isAdminLoginAttempt && !email.endsWith("@bshop.com")) {
            showError("Admin email must end with @bshop.com");
            return;
        }

        viewModel.login(email, password);
    }

    private void updateUIForAdminMode(boolean isAdmin) {
        binding.tilEmail.setHint(isAdmin ? "Admin Email" : "Email");
        binding.tilPassword.setHint(isAdmin ? "Admin Password" : "Password");
        binding.btnLogin.setText(isAdmin ? "Admin Login" : "Login");
        
        // Show/hide admin-specific UI elements
        binding.tvAdminNotice.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
        binding.btnRegister.setVisibility(isAdmin ? View.GONE : View.VISIBLE);
        binding.tvForgotPassword.setVisibility(isAdmin ? View.GONE : View.VISIBLE);
    }

    private void handleForgotPassword() {
        // In a real app, implement password recovery
        showMessage("Password recovery not implemented in demo");
    }

    private void updateLoadingState(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!isLoading);
        binding.switchAdminMode.setEnabled(!isLoading);
    }

    private void showError(String error) {
        Snackbar.make(binding.getRoot(), error, Snackbar.LENGTH_LONG)
            .setAction("OK", null)
            .show();
    }

    private void showMessage(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}