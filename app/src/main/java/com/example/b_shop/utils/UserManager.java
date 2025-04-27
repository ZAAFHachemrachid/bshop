package com.example.b_shop.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.b_shop.data.local.entities.UserRole;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Manages user session and authentication state
 * Provides centralized access to current user information
 * Handles role-based session management
 */
public class UserManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_LAST_ACTIVITY = "last_activity";
    private static final long ADMIN_SESSION_TIMEOUT_MINUTES = 30;
    
    private final SharedPreferences prefs;
    private final MutableLiveData<Boolean> isLoggedIn;
    private final MutableLiveData<Integer> currentUserId;
    private final MutableLiveData<Boolean> isAdminSession;
    private LocalDateTime lastActivityTime;

    private static volatile UserManager instance;

    private UserManager(Context context) {
        prefs = context.getApplicationContext()
                      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isLoggedIn = new MutableLiveData<>(isUserLoggedIn());
        currentUserId = new MutableLiveData<>(getUserId());
        isAdminSession = new MutableLiveData<>(false);
        if (isUserLoggedIn() && getUserRole() == UserRole.ADMIN) {
            initializeAdminSession();
        }
    }

    public static UserManager getInstance(Context context) {
        if (instance == null) {
            synchronized (UserManager.class) {
                if (instance == null) {
                    instance = new UserManager(context);
                }
            }
        }
        return instance;
    }

    public void loginUser(int userId, String email, String name, UserRole role) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.putString(KEY_USER_ROLE, role.name());
        editor.putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis());
        editor.apply();
        
        isLoggedIn.setValue(true);
        currentUserId.setValue(userId);

        if (role == UserRole.ADMIN) {
            initializeAdminSession();
        }
    }

    private void initializeAdminSession() {
        lastActivityTime = LocalDateTime.now();
        isAdminSession.setValue(true);
    }

    public void updateAdminActivity() {
        if (isAdminSession.getValue() == Boolean.TRUE) {
            lastActivityTime = LocalDateTime.now();
            prefs.edit()
                .putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
                .apply();
        }
    }

    public boolean validateAdminSession() {
        if (!isUserLoggedIn() || getUserRole() != UserRole.ADMIN) {
            return false;
        }

        if (lastActivityTime == null || 
            ChronoUnit.MINUTES.between(lastActivityTime, LocalDateTime.now()) >= ADMIN_SESSION_TIMEOUT_MINUTES) {
            logoutUser();
            return false;
        }

        updateAdminActivity();
        return true;
    }

    public void logoutUser() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        isLoggedIn.setValue(false);
        currentUserId.setValue(null);
        isAdminSession.setValue(false);
        lastActivityTime = null;
    }

    public boolean isUserLoggedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    public LiveData<Boolean> getLoginState() {
        return isLoggedIn;
    }

    public LiveData<Boolean> getAdminSessionState() {
        return isAdminSession;
    }

    public int getCurrentUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public LiveData<Integer> getCurrentUserIdLive() {
        return currentUserId;
    }

    private int getUserId() {
        return prefs.getInt(KEY_USER_ID, -1);
    }

    public String getCurrentUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getCurrentUserName() {
        return prefs.getString(KEY_USER_NAME, null);
    }

    public UserRole getUserRole() {
        String roleStr = prefs.getString(KEY_USER_ROLE, UserRole.USER.name());
        return UserRole.valueOf(roleStr);
    }

    /**
     * Validates if there is an active user session
     * For admin users, also checks session timeout
     * @throws IllegalStateException if no user is logged in or admin session has expired
     */
    public void validateUserSession() throws IllegalStateException {
        if (!isUserLoggedIn()) {
            throw new IllegalStateException("No active user session");
        }

        if (getUserRole() == UserRole.ADMIN && !validateAdminSession()) {
            throw new IllegalStateException("Admin session has expired");
        }
    }

    /**
     * Updates the current user's profile information
     */
    public void updateUserProfile(String name, String email) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
    }

    /**
     * Checks if the current user has admin privileges
     */
    public boolean isCurrentUserAdmin() {
        return isUserLoggedIn() && getUserRole() == UserRole.ADMIN && validateAdminSession();
    }
    
    /**
     * Gets the admin session timeout duration in minutes
     * @return The number of minutes after which an admin session expires
     */
    public static long getAdminSessionTimeoutMinutes() {
        return ADMIN_SESSION_TIMEOUT_MINUTES;
    }
}