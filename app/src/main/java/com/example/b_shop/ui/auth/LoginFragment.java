package com.example.b_shop.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.b_shop.R;
import com.example.b_shop.databinding.FragmentLoginBinding;
import com.example.b_shop.utils.ValidationUtils;
import com.google.android.material.textfield.TextInputLayout;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;
    private AuthViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AuthViewModelFactory factory = new AuthViewModelFactory(requireActivity().getApplication());
        viewModel = new ViewModelProvider(requireActivity(), factory).get(AuthViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.loginButton.setOnClickListener(v -> attemptLogin());
        
        binding.registerLink.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_login_to_register));
            
        // Check for saved credentials
        String savedEmail = viewModel.getSavedEmail();
        String savedPassword = viewModel.getSavedPassword();
        
        if (!savedEmail.isEmpty() && !savedPassword.isEmpty()) {
            binding.emailInput.setText(savedEmail);
            binding.passwordInput.setText(savedPassword);
            binding.rememberMeCheckbox.setChecked(true);
        }
    }

    private void observeViewModel() {
        viewModel.getLoginResult().observe(getViewLifecycleOwner(), result -> {
            hideLoading();
            
            if (result.isSuccess()) {
                navigateToMain();
            } else {
                handleError(result.getError());
            }
        });
    }

    private void attemptLogin() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        clearErrors();
        
        if (validateInput(email, password)) {
            showLoading();
            viewModel.login(email, password, binding.rememberMeCheckbox.isChecked());
        }
    }

    private boolean validateInput(String email, String password) {
        boolean isValid = true;
        
        // Validate email
        ValidationUtils.ValidationResult emailResult = ValidationUtils.validateEmail(email);
        if (!emailResult.isValid()) {
            binding.emailLayout.setError(emailResult.getError());
            isValid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        // Validate password
        ValidationUtils.ValidationResult passwordResult = ValidationUtils.validatePassword(password);
        if (!passwordResult.isValid()) {
            binding.passwordLayout.setError(passwordResult.getError());
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        return isValid;
    }

    private void navigateToMain() {
        Navigation.findNavController(requireView())
                 .navigate(R.id.action_login_to_main);
    }

    private void showLoading() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.loginButton.setEnabled(false);
        binding.emailInput.setEnabled(false);
        binding.passwordInput.setEnabled(false);
        binding.rememberMeCheckbox.setEnabled(false);
    }

    private void hideLoading() {
        binding.progressBar.setVisibility(View.GONE);
        binding.loginButton.setEnabled(true);
        binding.emailInput.setEnabled(true);
        binding.passwordInput.setEnabled(true);
        binding.rememberMeCheckbox.setEnabled(true);
    }

    private void handleError(String error) {
        if (error.toLowerCase().contains("email")) {
            binding.emailLayout.setError(error);
        } else if (error.toLowerCase().contains("password")) {
            binding.passwordLayout.setError(error);
        } else {
            binding.emailLayout.setError(null);
            binding.passwordLayout.setError(null);
            Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
        }
    }

    private void clearErrors() {
        binding.emailLayout.setError(null);
        binding.passwordLayout.setError(null);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clear observers to prevent memory leaks
        viewModel.getLoginResult().removeObservers(getViewLifecycleOwner());
        binding = null;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save input states if needed
        if (binding != null) {
            outState.putString("email", binding.emailInput.getText().toString());
            outState.putBoolean("remember_me", binding.rememberMeCheckbox.isChecked());
        }
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        // Restore input states if needed
        if (savedInstanceState != null && binding != null) {
            binding.emailInput.setText(savedInstanceState.getString("email", ""));
            binding.rememberMeCheckbox.setChecked(savedInstanceState.getBoolean("remember_me", false));
        }
    }
}