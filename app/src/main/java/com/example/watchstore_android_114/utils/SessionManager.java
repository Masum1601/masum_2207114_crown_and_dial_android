package com.example.watchstore_android_114.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SessionManager {
    private static final String PREF_NAME = "WatchStoreSession";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_IS_ADMIN = "isAdmin";

    private static SessionManager instance;
    private final SharedPreferences preferences;
    private final SharedPreferences.Editor editor;
    private final FirebaseAuth firebaseAuth;

    private SessionManager(Context context) {
        preferences = context.getApplicationContext()
                .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public void saveUserData(String username, boolean isAdmin) {
        editor.putString(KEY_USERNAME, username);
        editor.putBoolean(KEY_IS_ADMIN, isAdmin);
        editor.apply();
    }

    public boolean isLoggedIn() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null;
    }

    public String getUserId() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    public String getUsername() {
        return preferences.getString(KEY_USERNAME, null);
    }

    public String getEmail() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null ? currentUser.getEmail() : null;
    }

    public boolean isAdmin() {
        return preferences.getBoolean(KEY_IS_ADMIN, false);
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    public FirebaseAuth getFirebaseAuth() {
        return firebaseAuth;
    }

    public void logout() {
        firebaseAuth.signOut();
        editor.clear();
        editor.apply();
    }
}
