package com.example.b_shop.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Manages user session and authentication state
 * Provides centralized access to current user information
 */
public class UserManager {
    private static final String PREFS_NAME = "user_prefs";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_NAME = "user_name";
    
    private final SharedPreferences prefs;
    private final MutableLiveData<Boolean> isLoggedIn;
    private final MutableLiveData<Integer> currentUserId;

    private static volatile UserManager instance;

    private UserManager(Context context) {
        prefs = context.getApplicationContext()
                      .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        isLoggedIn = new MutableLiveData<>(isUserLoggedIn());
        currentUserId = new MutableLiveData<>(getUserId());
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

    public void loginUser(int userId, String email, String name) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_NAME, name);
        editor.apply();
        
        isLoggedIn.setValue(true);
        currentUserId.setValue(userId);
    }

    public void logoutUser() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
        
        isLoggedIn.setValue(false);
        currentUserId.setValue(null);
    }

    public boolean isUserLoggedIn() {
        return prefs.contains(KEY_USER_ID);
    }

    public LiveData<Boolean> getLoginState() {
        return isLoggedIn;
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

    /**
     * Validates if there is an active user session
     * @throws IllegalStateException if no user is logged in
     */
    public void validateUserSession() throws IllegalStateException {
        android.util.Log.d("UserManager", "Validating user session");
        if (!isUserLoggedIn()) {
            android.util.Log.w("UserManager", "No active user session found");
            throw new IllegalStateException("No active user session");
        }
        android.util.Log.d("UserManager", "Session validation successful - userId: " + getCurrentUserId());
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
}