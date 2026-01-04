package com.example.watchstore_android_114.utils;

import android.content.Context;
import android.util.Log;

import com.example.watchstore_android_114.models.CartItem;
import com.example.watchstore_android_114.models.Order;
import com.example.watchstore_android_114.models.User;
import com.example.watchstore_android_114.models.Watch;
import com.example.watchstore_android_114.models.WishlistItem;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class JSONDatabaseManager {
    private static final String TAG = "JSONDatabaseManager";
    private static JSONDatabaseManager instance;
    private final Context context;
    private final Gson gson;

    private static final String USERS_FILE = "users.json";
    private static final String WATCHES_FILE = "watches.json";
    private static final String CART_FILE = "cart.json";
    private static final String WISHLIST_FILE = "wishlist.json";
    private static final String ORDERS_FILE = "orders.json";

    private JSONDatabaseManager(Context context) {
        this.context = context.getApplicationContext();
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        initializeDatabase();
    }

    public static synchronized JSONDatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new JSONDatabaseManager(context);
        }
        return instance;
    }

    private void initializeDatabase() {
        if (!fileExists(USERS_FILE)) {
            createInitialUsers();
        }
        if (!fileExists(WATCHES_FILE)) {
            createInitialWatches();
        }
        if (!fileExists(CART_FILE)) {
            saveList(CART_FILE, new ArrayList<CartItem>());
        }
        if (!fileExists(WISHLIST_FILE)) {
            saveList(WISHLIST_FILE, new ArrayList<WishlistItem>());
        }
        if (!fileExists(ORDERS_FILE)) {
            saveList(ORDERS_FILE, new ArrayList<Order>());
        }
    }

    private boolean fileExists(String fileName) {
        File file = new File(context.getFilesDir(), fileName);
        return file.exists();
    }

    private void createInitialUsers() {
        List<User> users = new ArrayList<>();
        users.add(new User(1, "admin", "admin123", "admin@crowndia.com", true));
        users.add(new User(2, "user", "user123", "user@example.com", false));
        saveList(USERS_FILE, users);
        Log.d(TAG, "Initial users created");
    }

    private void createInitialWatches() {
        List<Watch> watches = new ArrayList<>();
        
        watches.add(new Watch(1, "Rolex Submariner", "Rolex", 8500.00, 
            "Iconic dive watch with exceptional craftsmanship", 15, "Luxury",
            "https://images.unsplash.com/photo-1523170335258-f5ed11844a49"));
        
        watches.add(new Watch(2, "Omega Seamaster", "Omega", 6200.00,
            "Professional diving watch with Co-Axial movement", 20, "Luxury",
            "https://images.unsplash.com/photo-1547996160-81dfa63595aa"));
        
        watches.add(new Watch(3, "Tag Heuer Carrera", "Tag Heuer", 4500.00,
            "Racing-inspired chronograph with precision engineering", 25, "Sport",
            "https://images.unsplash.com/photo-1587836374828-4dbafa94cf0e"));
        
        watches.add(new Watch(4, "Seiko Presage", "Seiko", 450.00,
            "Japanese craftsmanship with automatic movement", 50, "Classic",
            "https://images.unsplash.com/photo-1509941943102-10c232535736"));
        
        watches.add(new Watch(5, "Citizen Eco-Drive", "Citizen", 320.00,
            "Solar-powered watch with sustainable technology", 60, "Sport",
            "https://images.unsplash.com/photo-1524805444758-089113d48a6d"));
        
        watches.add(new Watch(6, "Apple Watch Series 9", "Apple", 399.00,
            "Advanced health tracking and smart features", 100, "Smart",
            "https://images.unsplash.com/photo-1579586337278-3befd40fd17a"));
        
        watches.add(new Watch(7, "Casio G-Shock", "Casio", 150.00,
            "Rugged and durable sports watch", 80, "Sport",
            "https://images.unsplash.com/photo-1585123388219-f40de5d74f57"));
        
        watches.add(new Watch(8, "Tissot PRX", "Tissot", 675.00,
            "Integrated bracelet watch with Swiss precision", 35, "Classic",
            "https://images.unsplash.com/photo-1622434641406-a158123450f9"));
        
        saveList(WATCHES_FILE, watches);
        Log.d(TAG, "Initial watches created");
    }

    private <T> void saveList(String fileName, List<T> list) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            FileWriter writer = new FileWriter(file);
            gson.toJson(list, writer);
            writer.close();
            Log.d(TAG, "Saved to " + fileName);
        } catch (IOException e) {
            Log.e(TAG, "Error saving " + fileName, e);
        }
    }

    private <T> List<T> loadList(String fileName, Type typeToken) {
        try {
            File file = new File(context.getFilesDir(), fileName);
            if (!file.exists()) {
                return new ArrayList<>();
            }
            FileReader reader = new FileReader(file);
            List<T> list = gson.fromJson(reader, typeToken);
            reader.close();
            return list != null ? list : new ArrayList<>();
        } catch (IOException e) {
            Log.e(TAG, "Error loading " + fileName, e);
            return new ArrayList<>();
        }
    }

    public List<User> getAllUsers() {
        Type type = new TypeToken<List<User>>(){}.getType();
        return loadList(USERS_FILE, type);
    }

    public User getUserByUsername(String username) {
        List<User> users = getAllUsers();
        for (User user : users) {
            if (user.getUsername().equals(username)) {
                return user;
            }
        }
        return null;
    }

    public boolean addUser(User user) {
        List<User> users = getAllUsers();
        user.setId(getNextUserId());
        users.add(user);
        saveList(USERS_FILE, users);
        return true;
    }

    private int getNextUserId() {
        List<User> users = getAllUsers();
        int maxId = 0;
        for (User user : users) {
            if (user.getId() > maxId) {
                maxId = user.getId();
            }
        }
        return maxId + 1;
    }

    public List<Watch> getAllWatches() {
        Type type = new TypeToken<List<Watch>>(){}.getType();
        return loadList(WATCHES_FILE, type);
    }

    public Watch getWatchById(int watchId) {
        List<Watch> watches = getAllWatches();
        for (Watch watch : watches) {
            if (watch.getId() == watchId) {
                return watch;
            }
        }
        return null;
    }

    public boolean addWatch(Watch watch) {
        List<Watch> watches = getAllWatches();
        watch.setId(getNextWatchId());
        watches.add(watch);
        saveList(WATCHES_FILE, watches);
        return true;
    }

    public boolean updateWatch(Watch updatedWatch) {
        List<Watch> watches = getAllWatches();
        for (int i = 0; i < watches.size(); i++) {
            if (watches.get(i).getId() == updatedWatch.getId()) {
                watches.set(i, updatedWatch);
                saveList(WATCHES_FILE, watches);
                return true;
            }
        }
        return false;
    }

    public boolean deleteWatch(int watchId) {
        List<Watch> watches = getAllWatches();
        watches.removeIf(watch -> watch.getId() == watchId);
        saveList(WATCHES_FILE, watches);
        return true;
    }

    private int getNextWatchId() {
        List<Watch> watches = getAllWatches();
        int maxId = 0;
        for (Watch watch : watches) {
            if (watch.getId() > maxId) {
                maxId = watch.getId();
            }
        }
        return maxId + 1;
    }

    public List<CartItem> getCartItems(int userId) {
        Type type = new TypeToken<List<CartItem>>(){}.getType();
        List<CartItem> allItems = loadList(CART_FILE, type);
        List<CartItem> userItems = new ArrayList<>();
        for (CartItem item : allItems) {
            if (item.getUserId() == userId) {
                userItems.add(item);
            }
        }
        return userItems;
    }

    public boolean addToCart(CartItem item) {
        Type type = new TypeToken<List<CartItem>>(){}.getType();
        List<CartItem> items = loadList(CART_FILE, type);
        item.setId(getNextCartId());
        items.add(item);
        saveList(CART_FILE, items);
        return true;
    }

    public boolean removeFromCart(int cartId) {
        Type type = new TypeToken<List<CartItem>>(){}.getType();
        List<CartItem> items = loadList(CART_FILE, type);
        items.removeIf(item -> item.getId() == cartId);
        saveList(CART_FILE, items);
        return true;
    }

    public boolean clearCart(int userId) {
        Type type = new TypeToken<List<CartItem>>(){}.getType();
        List<CartItem> items = loadList(CART_FILE, type);
        items.removeIf(item -> item.getUserId() == userId);
        saveList(CART_FILE, items);
        return true;
    }

    private int getNextCartId() {
        Type type = new TypeToken<List<CartItem>>(){}.getType();
        List<CartItem> items = loadList(CART_FILE, type);
        int maxId = 0;
        for (CartItem item : items) {
            if (item.getId() > maxId) {
                maxId = item.getId();
            }
        }
        return maxId + 1;
    }

    public List<WishlistItem> getWishlistItems(int userId) {
        Type type = new TypeToken<List<WishlistItem>>(){}.getType();
        List<WishlistItem> allItems = loadList(WISHLIST_FILE, type);
        List<WishlistItem> userItems = new ArrayList<>();
        for (WishlistItem item : allItems) {
            if (item.getUserId() == userId) {
                userItems.add(item);
            }
        }
        return userItems;
    }

    public boolean addToWishlist(WishlistItem item) {
        Type type = new TypeToken<List<WishlistItem>>(){}.getType();
        List<WishlistItem> items = loadList(WISHLIST_FILE, type);
        item.setId(getNextWishlistId());
        items.add(item);
        saveList(WISHLIST_FILE, items);
        return true;
    }

    public boolean removeFromWishlist(int wishlistId) {
        Type type = new TypeToken<List<WishlistItem>>(){}.getType();
        List<WishlistItem> items = loadList(WISHLIST_FILE, type);
        items.removeIf(item -> item.getId() == wishlistId);
        saveList(WISHLIST_FILE, items);
        return true;
    }

    private int getNextWishlistId() {
        Type type = new TypeToken<List<WishlistItem>>(){}.getType();
        List<WishlistItem> items = loadList(WISHLIST_FILE, type);
        int maxId = 0;
        for (WishlistItem item : items) {
            if (item.getId() > maxId) {
                maxId = item.getId();
            }
        }
        return maxId + 1;
    }

    public List<Order> getAllOrders() {
        Type type = new TypeToken<List<Order>>(){}.getType();
        return loadList(ORDERS_FILE, type);
    }

    public List<Order> getUserOrders(int userId) {
        List<Order> allOrders = getAllOrders();
        List<Order> userOrders = new ArrayList<>();
        for (Order order : allOrders) {
            if (order.getUserId() == userId) {
                userOrders.add(order);
            }
        }
        return userOrders;
    }

    public boolean addOrder(Order order) {
        List<Order> orders = getAllOrders();
        order.setId(getNextOrderId());
        orders.add(order);
        saveList(ORDERS_FILE, orders);
        return true;
    }

    public boolean updateOrderStatus(int orderId, String status) {
        List<Order> orders = getAllOrders();
        for (Order order : orders) {
            if (order.getId() == orderId) {
                order.setStatus(status);
                saveList(ORDERS_FILE, orders);
                return true;
            }
        }
        return false;
    }

    private int getNextOrderId() {
        List<Order> orders = getAllOrders();
        int maxId = 0;
        for (Order order : orders) {
            if (order.getId() > maxId) {
                maxId = order.getId();
            }
        }
        return maxId + 1;
    }
}
