package com.example.b_shop.utils;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import java.util.concurrent.TimeUnit;

/**
 * Manages admin user sessions with security features and timeout functionality.
 * Implements singleton pattern for global session state management.
 */
public class AdminSessionManager {
    private static final String PREFS_NAME = "admin_session_prefs";
    private static final String KEY_SESSION_TOKEN = "session_token";
    private static final String KEY_LAST_ACTIVITY = "last_activity";
    private static final String KEY_ADMIN_ID = "admin_id";
    private static final long SESSION_TIMEOUT_MINUTES = 30;
    
    private final SharedPreferences prefs;
    private final MutableLiveData<Boolean> isSessionValid;
    private final MutableLiveData<Long> sessionTimeRemaining;
    private static volatile AdminSessionManager instance;

    private AdminSessionManager(Context context) {
        prefs = context.getApplicationContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isSessionValid = new MutableLiveData<>(false);
        sessionTimeRemaining = new MutableLiveData<>();
        validateCurrentSession();
    }

    public static AdminSessionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (AdminSessionManager.class) {
                if (instance == null) {
                    instance = new AdminSessionManager(context);
                }
            }
        }
        return instance;
    }

    /**
     * Starts a new admin session
     */
    public void startSession(int adminId) {
        String sessionToken = SecurityUtils.generateSessionToken();
        long currentTime = System.currentTimeMillis();

        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_SESSION_TOKEN, sessionToken);
        editor.putLong(KEY_LAST_ACTIVITY, currentTime);
        editor.putInt(KEY_ADMIN_ID, adminId);
        editor.apply();

        isSessionValid.setValue(true);
        updateSessionTimeRemaining();
    }

    /**
     * Updates the last activity timestamp
     */
    public void updateLastActivity() {
        if (isSessionValid()) {
            prefs.edit()
                .putLong(KEY_LAST_ACTIVITY, System.currentTimeMillis())
                .apply();
            updateSessionTimeRemaining();
        }
    }

    /**
     * Validates the current session
     */
    public boolean validateCurrentSession() {
        if (!hasSession()) {
            invalidateSession();
            return false;
        }

        long lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, 0);
        long currentTime = System.currentTimeMillis();
        long sessionAge = currentTime - lastActivity;

        boolean valid = sessionAge < TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES);
        isSessionValid.setValue(valid);

        if (valid) {
            updateSessionTimeRemaining();
        } else {
            invalidateSession();
        }

        return valid;
    }

    /**
     * Ends the current session
     */
    public void endSession() {
        invalidateSession();
    }

    /**
     * Gets the current admin ID
     */
    public int getCurrentAdminId() {
        return prefs.getInt(KEY_ADMIN_ID, -1);
    }

    /**
     * Checks if there is an active session
     */
    public boolean hasSession() {
        return prefs.contains(KEY_SESSION_TOKEN) &&
               SecurityUtils.isValidTokenFormat(prefs.getString(KEY_SESSION_TOKEN, ""));
    }

    /**
     * Gets session validity state as LiveData
     */
    public LiveData<Boolean> getSessionValidState() {
        return isSessionValid;
    }

    /**
     * Gets remaining session time as LiveData
     */
    public LiveData<Long> getSessionTimeRemaining() {
        return sessionTimeRemaining;
    }

    /**
     * Gets the session timeout duration in minutes
     */
    public static long getSessionTimeoutMinutes() {
        return SESSION_TIMEOUT_MINUTES;
    }

    /**
     * Formats remaining time as string
     */
    @NonNull
    public String formatRemainingTime(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes);
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void invalidateSession() {
        prefs.edit().clear().apply();
        isSessionValid.setValue(false);
        sessionTimeRemaining.setValue(0L);
    }

    private boolean isSessionValid() {
        return isSessionValid.getValue() != null && isSessionValid.getValue();
    }

    private void updateSessionTimeRemaining() {
        if (!hasSession()) {
            sessionTimeRemaining.setValue(0L);
            return;
        }

        long lastActivity = prefs.getLong(KEY_LAST_ACTIVITY, 0);
        long currentTime = System.currentTimeMillis();
        long sessionAge = currentTime - lastActivity;
        long remaining = TimeUnit.MINUTES.toMillis(SESSION_TIMEOUT_MINUTES) - sessionAge;

        if (remaining <= 0) {
            invalidateSession();
        } else {
            sessionTimeRemaining.setValue(remaining);
        }
    }
}