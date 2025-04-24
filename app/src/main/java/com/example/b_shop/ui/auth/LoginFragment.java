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
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
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
            binding.progressBar.setVisibility(View.GONE);
            binding.loginButton.setEnabled(true);

            if (result.isSuccess()) {
                navigateToMain();
            } else {
                showError(result.getError());
            }
        });
    }

    private void attemptLogin() {
        String email = binding.emailInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString().trim();

        if (validateInput(email, password)) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.loginButton.setEnabled(false);
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

    private void showError(String error) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}