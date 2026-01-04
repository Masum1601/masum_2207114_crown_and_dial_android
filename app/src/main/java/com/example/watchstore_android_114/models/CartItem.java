package com.example.watchstore_android_114.models;

public class CartItem {
    private int id;
    private int userId;
    private int watchId;
    private String watchName;
    private String watchBrand;
    private double watchPrice;
    private int quantity;
    private int availableStock;
    private long addedAt;

    public CartItem() {
        this.addedAt = System.currentTimeMillis();
    }

    public CartItem(int id, int userId, int watchId, String watchName, String watchBrand,
                    double watchPrice, int quantity, int availableStock) {
        this.id = id;
        this.userId = userId;
        this.watchId = watchId;
        this.watchName = watchName;
        this.watchBrand = watchBrand;
        this.watchPrice = watchPrice;
        this.quantity = quantity;
        this.availableStock = availableStock;
        this.addedAt = System.currentTimeMillis();
    }

    public double getTotalPrice() {
        return watchPrice * quantity;
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

    public String getWatchName() {
        return watchName;
    }

    public void setWatchName(String watchName) {
        this.watchName = watchName;
    }

    public String getWatchBrand() {
        return watchBrand;
    }

    public void setWatchBrand(String watchBrand) {
        this.watchBrand = watchBrand;
    }

    public double getWatchPrice() {
        return watchPrice;
    }

    public void setWatchPrice(double watchPrice) {
        this.watchPrice = watchPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public void setAvailableStock(int availableStock) {
        this.availableStock = availableStock;
    }

    public long getAddedAt() {
        return addedAt;
    }

    public void setAddedAt(long addedAt) {
        this.addedAt = addedAt;
    }
}
