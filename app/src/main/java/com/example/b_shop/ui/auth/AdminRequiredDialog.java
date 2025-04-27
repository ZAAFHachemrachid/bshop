package com.example.b_shop.ui.auth;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.navigation.Navigation;
import com.example.b_shop.R;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

/**
 * Dialog shown when an operation requires admin authentication.
 * Provides options to proceed to admin login or cancel.
 */
public class AdminRequiredDialog extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String message = AdminRequiredDialogArgs.fromBundle(getArguments()).getMessage();

        return new MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.admin_access_required)
            .setMessage(message)
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(R.string.login, (dialog, which) -> {
                // Navigate to login with admin_required flag
                Navigation.findNavController(requireActivity(), R.id.nav_host_fragment)
                    .navigate(AdminRequiredDialogDirections
                        .actionAdminRequiredToLogin());
            })
            .setNegativeButton(R.string.cancel, (dialog, which) -> {
                // Just dismiss the dialog
                dialog.dismiss();
            })
            .create();
    }

    /**
     * Helper method to show the admin required dialog
     * 
     * @param fragment The fragment currently displayed
     * @param message Custom message explaining why admin access is needed
     */
    public static void show(@NonNull androidx.fragment.app.Fragment fragment, String message) {
        Bundle args = new Bundle();
        args.putString("message", message);
        Navigation.findNavController(fragment.requireView())
            .navigate(R.id.action_global_adminRequired, args);
    }
}