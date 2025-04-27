package com.example.b_shop.ui.auth;

import androidx.annotation.Nullable;
import com.example.b_shop.data.local.entities.UserRole;

/**
 * Represents the authentication state including loading, success, error states,
 * and user role information.
 */
public class AuthState {
    private final boolean loading;
    private final boolean success;
    private final UserRole role;
    private final String error;

    private AuthState(boolean loading, boolean success, @Nullable UserRole role, @Nullable String error) {
        this.loading = loading;
        this.success = success;
        this.role = role;
        this.error = error;
    }

    public boolean isLoading() {
        return loading;
    }

    public boolean isSuccess() {
        return success;
    }

    @Nullable
    public UserRole getRole() {
        return role;
    }

    @Nullable
    public String getError() {
        return error;
    }

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    /**
     * Factory method for initial state
     */
    public static AuthState initial() {
        return new AuthState(false, false, null, null);
    }

    /**
     * Factory method for loading state
     */
    public static AuthState loading() {
        return new AuthState(true, false, null, null);
    }

    /**
     * Factory method for success state
     */
    public static AuthState success(UserRole role) {
        return new AuthState(false, true, role, null);
    }

    /**
     * Factory method for error state
     */
    public static AuthState error(String error) {
        return new AuthState(false, false, null, error);
    }

    /**
     * Builder class for AuthState
     */
    public static class Builder {
        private boolean loading = false;
        private boolean success = false;
        private UserRole role = null;
        private String error = null;

        public Builder setLoading(boolean loading) {
            this.loading = loading;
            return this;
        }

        public Builder setSuccess(boolean success) {
            this.success = success;
            return this;
        }

        public Builder setRole(UserRole role) {
            this.role = role;
            return this;
        }

        public Builder setError(String error) {
            this.error = error;
            return this;
        }

        public AuthState build() {
            return new AuthState(loading, success, role, error);
        }
    }
}