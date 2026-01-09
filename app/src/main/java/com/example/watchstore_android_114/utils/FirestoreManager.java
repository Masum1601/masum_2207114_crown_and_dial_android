package com.example.watchstore_android_114.utils;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirestoreManager {
    
    private static FirestoreManager instance;
    private final FirebaseFirestore firestore;
    
    private FirestoreManager() {
        firestore = FirebaseFirestore.getInstance();
    }
    
    public static synchronized FirestoreManager getInstance() {
        if (instance == null) {
            instance = new FirestoreManager();
        }
        return instance;
    }
    
    public interface AddWatchCallback {
        void onSuccess(String watchId);
        void onFailure(String errorMessage);
    }
    
    public void addWatch(Map<String, Object> watchData, AddWatchCallback callback) {
        firestore.collection("watches")
            .add(watchData)
            .addOnSuccessListener(documentReference -> {
                callback.onSuccess(documentReference.getId());
            })
            .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }
    
    public interface GetWatchesCallback {
        void onSuccess(List<Map<String, Object>> watches);
        void onFailure(String errorMessage);
    }
    
    public void getAllWatches(GetWatchesCallback callback) {
        firestore.collection("watches")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> watchList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> watch = new HashMap<>(document.getData());
                    watch.put("id", document.getId());
                    watchList.add(watch);
                }
                callback.onSuccess(watchList);
            })
            .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }
    
    public interface GetWatchCallback {
        void onSuccess(Map<String, Object> watch);
        void onFailure(String errorMessage);
    }
    
    public void getWatch(String watchId, GetWatchCallback callback) {
        firestore.collection("watches")
            .document(watchId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> watch = new HashMap<>(documentSnapshot.getData());
                    watch.put("id", documentSnapshot.getId());
                    callback.onSuccess(watch);
                } else {
                    callback.onFailure("Watch not found");
                }
            })
            .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }
    
    public interface UpdateWatchCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
    
    public void updateWatch(String watchId, Map<String, Object> updates, 
                           UpdateWatchCallback callback) {
        firestore.collection("watches")
            .document(watchId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void deleteWatch(String watchId, UpdateWatchCallback callback) {
        firestore.collection("watches")
            .document(watchId)
            .delete()
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void getWatchesByBrand(String brand, GetWatchesCallback callback) {
        firestore.collection("watches")
            .whereEqualTo("brand", brand)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> watchList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> watch = new HashMap<>(document.getData());
                    watch.put("id", document.getId());
                    watchList.add(watch);
                }
                callback.onSuccess(watchList);
            })
            .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }
    
    public void getWatchesByPriceRange(double minPrice, double maxPrice, 
                                       GetWatchesCallback callback) {
        firestore.collection("watches")
            .whereGreaterThanOrEqualTo("price", minPrice)
            .whereLessThanOrEqualTo("price", maxPrice)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> watchList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> watch = new HashMap<>(document.getData());
                    watch.put("id", document.getId());
                    watchList.add(watch);
                }
                callback.onSuccess(watchList);
            })
            .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }
    
    public void addToFavorites(String userId, String watchId, 
                              UpdateWatchCallback callback) {
        Map<String, Object> favorite = new HashMap<>();
        favorite.put("userId", userId);
        favorite.put("watchId", watchId);
        favorite.put("createdAt", System.currentTimeMillis());
        
        firestore.collection("favorites")
            .add(favorite)
            .addOnSuccessListener(documentReference -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void getUserFavorites(String userId, GetWatchesCallback callback) {
        firestore.collection("favorites")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<String> watchIds = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String watchId = document.getString("watchId");
                    if (watchId != null) {
                        watchIds.add(watchId);
                    }
                }
                
                if (!watchIds.isEmpty()) {
                    fetchWatchesByIds(watchIds, callback);
                } else {
                    callback.onSuccess(new ArrayList<>());
                }
            })
            .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }
    
    private void fetchWatchesByIds(List<String> watchIds, GetWatchesCallback callback) {
        firestore.collection("watches")
            .whereIn("__name__", watchIds)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Map<String, Object>> watchList = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Map<String, Object> watch = new HashMap<>(document.getData());
                    watch.put("id", document.getId());
                    watchList.add(watch);
                }
                callback.onSuccess(watchList);
            })
            .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }
}
