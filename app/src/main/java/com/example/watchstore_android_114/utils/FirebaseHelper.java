package com.example.watchstore_android_114.utils;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.HashMap;
import java.util.Map;

public class FirebaseHelper {
    
    private static FirebaseHelper instance;
    private final FirebaseAuth firebaseAuth;
    private final FirebaseFirestore firestore;
    
    private FirebaseHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
    }
    
    public static synchronized FirebaseHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseHelper();
        }
        return instance;
    }
    
    public FirebaseAuth getAuth() {
        return firebaseAuth;
    }
    
    public FirebaseFirestore getFirestore() {
        return firestore;
    }
    
    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }
    
    public boolean isUserLoggedIn() {
        return getCurrentUser() != null;
    }
    
    public interface AuthCallback {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }
    
    public void registerUser(String email, String password, String username, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    if (user != null) {
                        createUserDocument(user.getUid(), username, email, false);
                        callback.onSuccess(user);
                    }
                } else {
                    String errorMessage = task.getException() != null 
                        ? task.getException().getMessage() 
                        : "Registration failed";
                    callback.onFailure(errorMessage);
                }
            });
    }
    
    public void loginUser(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    FirebaseUser user = firebaseAuth.getCurrentUser();
                    callback.onSuccess(user);
                } else {
                    String errorMessage = task.getException() != null 
                        ? task.getException().getMessage() 
                        : "Login failed";
                    callback.onFailure(errorMessage);
                }
            });
    }
    
    public void createUserDocument(String userId, String username, String email, boolean isAdmin) {
        Map<String, Object> user = new HashMap<>();
        user.put("username", username);
        user.put("email", email);
        user.put("isAdmin", isAdmin);
        user.put("createdAt", System.currentTimeMillis());
        
        firestore.collection("users")
            .document(userId)
            .set(user)
            .addOnSuccessListener(aVoid -> {
            })
            .addOnFailureListener(e -> {
            });
    }
    
    public interface UserDataCallback {
        void onSuccess(Map<String, Object> userData);
        void onFailure(String errorMessage);
    }
    
    public void getUserData(String userId, UserDataCallback callback) {
        firestore.collection("users")
            .document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    Map<String, Object> userData = documentSnapshot.getData();
                    callback.onSuccess(userData);
                } else {
                    callback.onFailure("User not found");
                }
            })
            .addOnFailureListener(e -> {
                callback.onFailure(e.getMessage());
            });
    }
    
    public void updateUserProfile(String userId, Map<String, Object> updates, 
                                  FirestoreCallback callback) {
        firestore.collection("users")
            .document(userId)
            .update(updates)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public void signOut() {
        firebaseAuth.signOut();
    }
    
    public void sendPasswordResetEmail(String email, FirestoreCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
            .addOnSuccessListener(aVoid -> callback.onSuccess())
            .addOnFailureListener(e -> callback.onFailure(e.getMessage()));
    }
    
    public interface FirestoreCallback {
        void onSuccess();
        void onFailure(String errorMessage);
    }
}
