package com.example.watchstore_android_114.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "WatchStoreSession";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_EMAIL = "email";
    private static final String KEY_IS_ADMIN = "isAdmin";

    private static SessionManager instance;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;

    private SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void createSession(int userId, String username, String email, boolean isAdmin) {
        editor.putBoolean(KEY_IS_LOGGED_IN, true);
        editor.putInt(KEY_USER_ID, userId);
        editor.putString(KEY_USERNAME, username);
        editor.putString(KEY_EMAIL, email);
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply();
    }

    public boolean isLoggedIn() {
        return preferences.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public int getUserId() {
        return preferences.getInt(KEY_USER_ID, -1);
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        return preferences.getString(KEY_EMAIL, null);
    }

    public boolean isAdmin() {
        return preferences.getBoolean(KEY_IS_ADMIN, false);
    }

    public void logout() {
        editor.clear();
        editor.apply();
    }
}
