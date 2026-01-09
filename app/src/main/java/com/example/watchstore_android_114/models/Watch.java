package com.example.watchstore_android_114.models;

import java.util.HashMap;
import java.util.Map;

public class Watch {
    private String id;
    private String name;
    private String brand;
    private double price;
    private String description;
    private int stock;
    private String category;
    private String imageUrl;
    private long createdAt;

    public Watch() {
        this.createdAt = System.currentTimeMillis();
    }

    public Watch(String id, String name, String brand, double price, String description, 
                 int stock, String category, String imageUrl) {
        this.id = id;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.description = description;
        this.stock = stock;
        this.category = category;
        this.imageUrl = imageUrl;
        this.createdAt = System.currentTimeMillis();
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("name", name);
        map.put("brand", brand);
        map.put("price", price);
        map.put("description", description);
        map.put("stock", stock);
        map.put("category", category);
        map.put("imageUrl", imageUrl);
        map.put("createdAt", createdAt);
        return map;
    }
    
    public static Watch fromMap(String id, Map<String, Object> map) {
        Watch watch = new Watch();
        watch.setId(id);
        watch.setName((String) map.get("name"));
        watch.setBrand((String) map.get("brand"));
        
        Object priceObj = map.get("price");
        if (priceObj instanceof Number) {
            watch.setPrice(((Number) priceObj).doubleValue());
        }
        
        watch.setDescription((String) map.get("description"));
        
        Object stockObj = map.get("stock");
        if (stockObj instanceof Number) {
            watch.setStock(((Number) stockObj).intValue());
        }
        
        watch.setCategory((String) map.get("category"));
        watch.setImageUrl((String) map.get("imageUrl"));
        
        Object createdAtObj = map.get("createdAt");
        if (createdAtObj instanceof Number) {
            watch.setCreatedAt(((Number) createdAtObj).longValue());
        }
        
        return watch;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
