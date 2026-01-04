package com.example.watchstore_android_114.models;

public class WishlistItem {
    private int id;
    private int userId;
    private int watchId;
    private long addedAt;

    public WishlistItem() {
        this.addedAt = System.currentTimeMillis();
    }

    public WishlistItem(int id, int userId, int watchId) {
        this.id = id;
        this.userId = userId;
        this.watchId = watchId;
        this.addedAt = System.currentTimeMillis();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public int getWatchId() {
        return watchId;
    }

    public void setWatchId(int watchId) {
        this.watchId = watchId;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
}
