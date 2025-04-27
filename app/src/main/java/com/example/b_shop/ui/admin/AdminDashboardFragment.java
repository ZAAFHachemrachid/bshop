package com.example.b_shop.ui.admin;

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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.b_shop.R;
import com.example.b_shop.data.local.entities.UserRole;
import com.example.b_shop.data.repositories.UserRepository;
import com.example.b_shop.databinding.FragmentAdminDashboardBinding;
import com.example.b_shop.databinding.DialogCreateAdminBinding;
import com.example.b_shop.utils.UserManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import java.util.concurrent.TimeUnit;

public class AdminDashboardFragment extends Fragment {
    private FragmentAdminDashboardBinding binding;
    private AdminViewModel viewModel;
    private UserManager userManager;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userManager = UserManager.getInstance(requireContext());
        
        // Verify admin access
        if (!userManager.isCurrentUserAdmin()) {
            Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                .navigate(R.id.action_adminDashboard_to_home);
            Toast.makeText(requireContext(), "Admin access required", Toast.LENGTH_SHORT).show();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                           @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupViewModel();
        setupUI();
        observeViewModel();
    }

    private void setupViewModel() {
        UserRepository userRepository = UserRepository.getInstance(requireContext());
        AdminViewModelFactory factory = AdminViewModelFactory.getInstance(
            requireActivity().getApplication(),
            userRepository,
            userManager
        );
        
        viewModel = new ViewModelProvider(this, factory).get(AdminViewModel.class);
    }

    private void setupUI() {
        // Setup navigation to different admin sections
        binding.btnUserManagement.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_adminDashboard_to_userManagement));

        binding.btnOrderManagement.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_adminDashboard_to_orderManagement));

        binding.btnAnalytics.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_adminDashboard_to_adminAnalytics));

        // Setup quick actions
        binding.fabCreateAdmin.setOnClickListener(v -> showCreateAdminDialog());

        // Setup recent activity recycler view
        binding.rvRecentActivity.setLayoutManager(new LinearLayoutManager(requireContext()));
        RecentActivityAdapter adapter = new RecentActivityAdapter();
        binding.rvRecentActivity.setAdapter(adapter);

        // Load recent audit logs
        viewModel.getRecentAuditLogs(10).observe(getViewLifecycleOwner(), auditLogs -> {
            adapter.submitList(auditLogs);
            binding.emptyView.setVisibility(auditLogs.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Setup active users counter
        viewModel.getActiveUsers(UserRole.USER, 24).observe(getViewLifecycleOwner(), users -> {
            binding.tvActiveUsers.setText(String.valueOf(users.size()));
        });
    }

    private void observeViewModel() {
        viewModel.getOperationResult().observe(getViewLifecycleOwner(), result -> {
            if (result.isSuccess()) {
                Snackbar.make(binding.getRoot(), result.getMessage(), Snackbar.LENGTH_SHORT).show();
            } else {
                new MaterialAlertDialogBuilder(requireContext())
                    .setTitle("Operation Failed")
                    .setMessage(result.getMessage())
                    .setPositiveButton("OK", null)
                    .show();
            }
        });
    }

    private void showCreateAdminDialog() {
        DialogCreateAdminBinding dialogBinding = DialogCreateAdminBinding.inflate(getLayoutInflater());
        
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle("Create Admin User")
            .setView(dialogBinding.getRoot())
            .setPositiveButton("Create", (dialog, which) -> {
                String name = dialogBinding.etAdminName.getText().toString().trim();
                String email = dialogBinding.etAdminEmail.getText().toString().trim();
                String password = dialogBinding.etAdminPassword.getText().toString().trim();

                if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                viewModel.createAdminUser(email, name, password);
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}