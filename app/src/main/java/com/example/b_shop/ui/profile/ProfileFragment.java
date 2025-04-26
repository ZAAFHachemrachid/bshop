package com.example.b_shop.ui.profile;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.example.b_shop.BShopApplication;
import com.example.b_shop.R;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.utils.UserManager;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextView tvUserName;
    private TextView tvUserEmail;
    private ProgressBar progressBar;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                         Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize views and check for null
        tvUserName = view.findViewById(R.id.tv_user_name);
        tvUserEmail = view.findViewById(R.id.tv_user_email);
        progressBar = view.findViewById(R.id.progress_bar);

        if (tvUserName == null || tvUserEmail == null || progressBar == null) {
            Toast.makeText(requireContext(), "Error initializing views", Toast.LENGTH_SHORT).show();
            return;
        }

        setupViewModel();
        setupClickListeners(view);
        observeViewModel();
    }

    private void setupViewModel() {
        BShopApplication application = (BShopApplication) requireActivity().getApplication();
        ProfileViewModelFactory factory = new ProfileViewModelFactory(application);
        viewModel = new ViewModelProvider(this, factory).get(ProfileViewModel.class);
    }

    private void setupClickListeners(View view) {
        view.findViewById(R.id.btn_edit_profile).setOnClickListener(v -> {
            // Navigate to edit profile (to be implemented)
            Toast.makeText(requireContext(), "Edit Profile coming soon", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_my_orders).setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_profile_to_orders);
        });

        view.findViewById(R.id.btn_settings).setOnClickListener(v -> {
            // Navigate to settings (to be implemented)
            Toast.makeText(requireContext(), "Settings coming soon", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_logout).setOnClickListener(v -> {
            viewModel.logout();
            Navigation.findNavController(v).navigate(R.id.action_profile_to_auth);
        });
    }

    private void observeViewModel() {
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null && tvUserName != null && tvUserEmail != null) {
                String name = user.getName();
                String email = user.getEmail();
                if (name != null) {
                    tvUserName.setText(name);
                }
                if (email != null) {
                    tvUserEmail.setText(email);
                }
            }
        });

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (progressBar != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && isAdded()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                viewModel.clearError();
            }
        });
    }
}