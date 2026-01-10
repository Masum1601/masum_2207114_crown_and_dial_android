package com.example.watchstore_android_114.models;

import java.util.ArrayList;
import java.util.List;

public class Order {
    private int id;
    private int userId;
    private String username;
    private double totalAmount;
    private String status;
    private long orderDate;
    private List<OrderItem> items;

    public Order() {
        this.orderDate = System.currentTimeMillis();
        this.status = "Pending";
        this.items = new ArrayList<>();
    }

    public Order(int id, int userId, String username, double totalAmount, String status) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.totalAmount = totalAmount;
        this.status = status;
        this.orderDate = System.currentTimeMillis();
        this.items = new ArrayList<>();
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(long orderDate) {
        this.orderDate = orderDate;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public static class OrderItem {
        private String watchId;
        private String watchName;
        private int quantity;
        private double price;

        public OrderItem() {}

        public OrderItem(String watchId, String watchName, int quantity, double price) {
            this.watchId = watchId;
            this.watchName = watchName;
            this.quantity = quantity;
            this.price = price;
        }

        public String getWatchId() {
            return watchId;
        }

        public void setWatchId(String watchId) {
            this.watchId = watchId;
        }

        public String getWatchName() {
            return watchName;
        }

        public void setWatchName(String watchName) {
            this.watchName = watchName;
        }

        public int getQuantity() {
            return quantity;
        }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }
}
