package com.example.b_shop.utils;

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import com.example.b_shop.R;
import com.example.b_shop.ui.auth.AdminRequiredDialog;
import com.example.b_shop.ui.auth.AuthActivity;
import java.util.regex.Pattern;

/**
 * Utility class for admin-related operations and validations.
 */
public class AdminUtils {
    private static final String ADMIN_EMAIL_DOMAIN = "@bshop.com";
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*])(?=\\S+$).{8,}$"
    );

    /**
     * Validates if an email follows admin email format
     */
    public static boolean isValidAdminEmail(@NonNull String email) {
        return email.toLowerCase().endsWith(ADMIN_EMAIL_DOMAIN);
    }

    /**
     * Validates admin password complexity
     */
    public static boolean isValidAdminPassword(@NonNull String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            return false;
        }
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Gets password requirements message
     */
    public static String getPasswordRequirements(Context context) {
        return context.getString(R.string.password_requirements);
    }

    /**
     * Handles unauthorized admin access attempt
     */
    public static void handleUnauthorizedAdminAccess(Fragment fragment, String operation) {
        AdminRequiredDialog.show(fragment, 
            fragment.getString(R.string.admin_access_required) + 
            " for " + operation);
    }

    /**
     * Handles expired admin session
     */
    public static void handleExpiredAdminSession(Context context) {
        Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra("admin_required", true);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

    /**
     * Creates a secure random admin password
     */
    public static String generateSecureAdminPassword() {
        // Implementation in SecurityUtils for better organization
        return SecurityUtils.generateSecurePassword();
    }

    /**
     * Validates admin action based on rules and permissions
     */
    public static boolean isValidAdminAction(String action, String adminEmail) {
        // Basic validation
        if (!isValidAdminEmail(adminEmail)) {
            return false;
        }

        // Add more sophisticated validation based on action type
        switch (action) {
            case "DELETE_USER":
            case "CHANGE_ROLE":
            case "SYSTEM_CONFIG":
                // These actions might require additional verification
                return isHighPrivilegeAdmin(adminEmail);
            default:
                return true;
        }
    }

    /**
     * Checks if admin has high privilege level
     * In a real app, this would check against a more sophisticated role system
     */
    private static boolean isHighPrivilegeAdmin(String adminEmail) {
        return adminEmail.startsWith("super.") || 
               adminEmail.startsWith("system.") ||
               adminEmail.startsWith("root.");
    }

    /**
     * Gets user-friendly description of admin action
     */
    public static String getActionDescription(String action) {
        switch (action) {
            case "USER_CREATED":
                return "Created new user";
            case "USER_UPDATED":
                return "Updated user details";
            case "USER_DELETED":
                return "Deleted user";
            case "ROLE_CHANGED":
                return "Changed user role";
            case "USER_BLOCKED":
                return "Blocked user";
            case "USER_UNBLOCKED":
                return "Unblocked user";
            case "SYSTEM_CONFIG":
                return "Modified system configuration";
            default:
                return action.replace("_", " ").toLowerCase();
        }
    }
}