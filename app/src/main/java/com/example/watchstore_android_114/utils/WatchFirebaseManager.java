package com.example.watchstore_android_114.utils;

import com.example.watchstore_android_114.models.Watch;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WatchFirebaseManager {
    
    private static WatchFirebaseManager instance;
    private final FirebaseFirestore firestore;
    
    private WatchFirebaseManager() {
        firestore = FirebaseFirestore.getInstance();
    }
    
    public static synchronized WatchFirebaseManager getInstance() {
        if (instance == null) {
            instance = new WatchFirebaseManager();
        }
        return instance;
    }

    public void addWatch(Watch watch, WatchIdCallback callback) {
        firestore.collection("watches")
                .add(watch.toMap())
                .addOnSuccessListener(documentReference -> {
                    String watchId = documentReference.getId();
                    callback.onSuccess(watchId);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void getWatch(String watchId, WatchCallback callback) {
        firestore.collection("watches")
                .document(watchId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Map<String, Object> data = documentSnapshot.getData();
                        if (data != null) {
                            Watch watch = Watch.fromMap(documentSnapshot.getId(), data);
                            callback.onSuccess(watch);
                        } else {
                            callback.onFailure("Watch data is empty");
                        }
                    } else {
                        callback.onFailure("Watch not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void getAllWatches(WatchesCallback callback) {
        firestore.collection("watches")
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Watch> watchList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Watch watch = Watch.fromMap(document.getId(), document.getData());
                        watchList.add(watch);
                    }
                    callback.onSuccess(watchList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void updateWatch(String watchId, Map<String, Object> updates, SuccessCallback callback) {
        firestore.collection("watches")
                .document(watchId)
                .update(updates)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void updateWatchFull(String watchId, Watch watch, SuccessCallback callback) {
        firestore.collection("watches")
                .document(watchId)
                .set(watch.toMap())
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void deleteWatch(String watchId, SuccessCallback callback) {
        firestore.collection("watches")
                .document(watchId)
                .delete()
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public void getWatchesByBrand(String brand, WatchesCallback callback) {
        firestore.collection("watches")
                .whereEqualTo("brand", brand)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Watch> watchList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Watch watch = Watch.fromMap(document.getId(), document.getData());
                        watchList.add(watch);
                    }
                    callback.onSuccess(watchList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void getWatchesByCategory(String category, WatchesCallback callback) {
        firestore.collection("watches")
                .whereEqualTo("category", category)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Watch> watchList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Watch watch = Watch.fromMap(document.getId(), document.getData());
                        watchList.add(watch);
                    }
                    callback.onSuccess(watchList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void getWatchesByPriceRange(double minPrice, double maxPrice, WatchesCallback callback) {
        firestore.collection("watches")
                .whereGreaterThanOrEqualTo("price", minPrice)
                .whereLessThanOrEqualTo("price", maxPrice)
                .orderBy("price", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Watch> watchList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Watch watch = Watch.fromMap(document.getId(), document.getData());
                        watchList.add(watch);
                    }
                    callback.onSuccess(watchList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void getWatchesSortedByPrice(boolean ascending, WatchesCallback callback) {
        Query.Direction direction = ascending ? Query.Direction.ASCENDING : Query.Direction.DESCENDING;
        
        firestore.collection("watches")
                .orderBy("price", direction)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Watch> watchList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Watch watch = Watch.fromMap(document.getId(), document.getData());
                        watchList.add(watch);
                    }
                    callback.onSuccess(watchList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void getAvailableWatches(WatchesCallback callback) {
        firestore.collection("watches")
                .whereGreaterThan("stock", 0)
                .orderBy("stock", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Watch> watchList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Watch watch = Watch.fromMap(document.getId(), document.getData());
                        watchList.add(watch);
                    }
                    callback.onSuccess(watchList);
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void updateWatchStock(String watchId, int newStock, SuccessCallback callback) {
        firestore.collection("watches")
                .document(watchId)
                .update("stock", newStock)
                .addOnSuccessListener(aVoid -> callback.onSuccess())
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void decreaseWatchStock(String watchId, int quantity, SuccessCallback callback) {
        firestore.collection("watches")
                .document(watchId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Long currentStock = documentSnapshot.getLong("stock");
                        if (currentStock != null && currentStock >= quantity) {
                            int newStock = currentStock.intValue() - quantity;
                            updateWatchStock(watchId, newStock, callback);
                        } else {
                            callback.onFailure("Insufficient stock");
                        }
                    } else {
                        callback.onFailure("Watch not found");
                    }
                })
                .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }

    public interface SuccessCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
    
    public interface WatchCallback {
        void onSuccess(Watch watch);
        void onFailure(String errorMessage);
    }
    
    public interface WatchesCallback {
        void onSuccess(List<Watch> watches);
        void onFailure(String errorMessage);
    }
    
    public interface WatchIdCallback {
        void onSuccess(String watchId);
        void onFailure(String errorMessage);
    }
}
