package com.example.b_shop.ui.profile;

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
import com.example.b_shop.databinding.FragmentProfileBinding;
import com.example.b_shop.utils.UserManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

public class ProfileFragment extends Fragment {
    private FragmentProfileBinding binding;
    private ProfileViewModel viewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UserManager userManager = UserManager.getInstance(requireContext());
        ProfileViewModelFactory factory = ProfileViewModelFactory.getInstance(
            requireActivity().getApplication(),
            userManager
        );
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupUI();
        observeViewModel();
    }

    private void setupUI() {
        if (!viewModel.isLoggedIn()) {
            navigateToAuth();
            return;
        }

        binding.tvUserName.setText(viewModel.getCurrentUserName());
        binding.tvUserEmail.setText(viewModel.getCurrentUserEmail());

        binding.btnLogout.setOnClickListener(v -> showLogoutConfirmation());
        binding.btnOrders.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_profile_to_orders));

        // Admin dashboard button is initially hidden
        binding.btnAdminDashboard.setVisibility(View.GONE);
    }

    private void observeViewModel() {
        viewModel.getIsAdminUser().observe(getViewLifecycleOwner(), isAdmin -> {
            binding.btnAdminDashboard.setVisibility(isAdmin ? View.VISIBLE : View.GONE);
            if (isAdmin) {
                setupAdminDashboardAccess();
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                showError(error);
            }
        });
    }

    private void setupAdminDashboardAccess() {
        binding.btnAdminDashboard.setOnClickListener(v -> {
            if (viewModel.canAccessAdminDashboard()) {
                if (viewModel.validateAdminSession()) {
                    Navigation.findNavController(v)
                        .navigate(R.id.action_profile_to_adminDashboard);
                } else {
                    showError(getString(R.string.admin_session_expired));
                    navigateToAuth();
                }
            } else {
                showError(getString(R.string.admin_access_required));
            }
        });
    }

    private void showLogoutConfirmation() {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout", (dialog, which) -> {
                viewModel.logout();
                navigateToAuth();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void navigateToAuth() {
        Navigation.findNavController(requireView())
            .navigate(R.id.action_profile_to_auth);
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}