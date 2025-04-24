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
import com.example.b_shop.databinding.FragmentRegisterBinding;
import com.example.b_shop.utils.ValidationUtils;

public class RegisterFragment extends Fragment {

    private FragmentRegisterBinding binding;
    private AuthViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(requireActivity()).get(AuthViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        binding.registerButton.setOnClickListener(v -> attemptRegistration());
        binding.loginLink.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_register_to_login));
    }

    private void observeViewModel() {
        viewModel.getRegistrationResult().observe(getViewLifecycleOwner(), result -> {
            binding.progressBar.setVisibility(View.GONE);
            binding.registerButton.setEnabled(true);

            if (result.isSuccess()) {
                navigateToMain();
            } else {
                showError(result.getError());
            }
        });
    }

    private void attemptRegistration() {
        String name = binding.nameInput.getText().toString().trim();
        String email = binding.emailInput.getText().toString().trim();
        String phone = binding.phoneInput.getText().toString().trim();
        String password = binding.passwordInput.getText().toString();
        String confirmPassword = binding.confirmPasswordInput.getText().toString();

        if (validateInput(name, email, phone, password, confirmPassword)) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.registerButton.setEnabled(false);
            viewModel.register(email, name, phone, password);
        }
    }

    private boolean validateInput(String name, String email, String phone, String password, String confirmPassword) {
        boolean isValid = true;

        // Validate name
        ValidationUtils.ValidationResult nameResult = ValidationUtils.validateName(name);
        if (!nameResult.isValid()) {
            binding.nameLayout.setError(nameResult.getError());
            isValid = false;
        } else {
            binding.nameLayout.setError(null);
        }

        // Validate email
        ValidationUtils.ValidationResult emailResult = ValidationUtils.validateEmail(email);
        if (!emailResult.isValid()) {
            binding.emailLayout.setError(emailResult.getError());
            isValid = false;
        } else {
            binding.emailLayout.setError(null);
        }

        // Validate phone
        ValidationUtils.ValidationResult phoneResult = ValidationUtils.validatePhone(phone);
        if (!phoneResult.isValid()) {
            binding.phoneLayout.setError(phoneResult.getError());
            isValid = false;
        } else {
            binding.phoneLayout.setError(null);
            // Format phone number if valid
            binding.phoneInput.setText(ValidationUtils.formatPhoneNumber(phone));
        }

        // Validate password
        ValidationUtils.ValidationResult passwordResult = ValidationUtils.validatePassword(password);
        if (!passwordResult.isValid()) {
            binding.passwordLayout.setError(passwordResult.getError());
            isValid = false;
        } else {
            binding.passwordLayout.setError(null);
        }

        // Validate confirm password
        ValidationUtils.ValidationResult confirmResult = ValidationUtils.validatePasswordMatch(password, confirmPassword);
        if (!confirmResult.isValid()) {
            binding.confirmPasswordLayout.setError(confirmResult.getError());
            isValid = false;
        } else {
            binding.confirmPasswordLayout.setError(null);
        }

        return isValid;
    }

    private void navigateToMain() {
        Navigation.findNavController(requireView())
                 .navigate(R.id.action_register_to_main);
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