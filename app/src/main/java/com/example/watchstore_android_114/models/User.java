package com.example.watchstore_android_114.models;

public class User {
    private String userId;
    private String username;
    private String email;
    private boolean isAdmin;
    private long createdAt;

    public User() {
        this.createdAt = System.currentTimeMillis();
    }

    public User(String userId, String username, String email, boolean isAdmin) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.isAdmin = isAdmin;
        this.createdAt = System.currentTimeMillis();
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
