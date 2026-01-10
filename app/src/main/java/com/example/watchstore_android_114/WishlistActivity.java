package com.example.watchstore_android_114;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchstore_android_114.models.Watch;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class WishlistActivity extends AppCompatActivity {

    private RecyclerView rvWishlist;
    private WishlistAdapter wishlistAdapter;
    private List<Watch> wishlistWatches = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoItems, tvWishlistCount;
    private Button btnBack;

    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadWishlistItems();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Wishlist");
        }
    }

    private void initializeViews() {
        rvWishlist = findViewById(R.id.rv_wishlist);
        progressBar = findViewById(R.id.progress_bar);
        tvNoItems = findViewById(R.id.tv_no_items);
        tvWishlistCount = findViewById(R.id.tv_wishlist_count);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        wishlistAdapter = new WishlistAdapter(this, wishlistWatches, new WishlistAdapter.OnWishlistActionListener() {
            @Override
            public void onRemoveFromWishlist(Watch watch, String wishlistItemId) {
                removeFromWishlist(wishlistItemId);
            }

            @Override
            public void onViewDetails(Watch watch) {
                android.content.Intent intent = new android.content.Intent(WishlistActivity.this, WatchDetailsActivity.class);
                intent.putExtra("WATCH_ID", watch.getId());
                startActivity(intent);
            }
        });

        rvWishlist.setLayoutManager(new LinearLayoutManager(this));
        rvWishlist.setAdapter(wishlistAdapter);
    }

    private void loadWishlistItems() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvNoItems.setVisibility(View.GONE);

        db.collection("wishlist")
            .whereEqualTo("userId", userId)
            .addSnapshotListener((queryDocumentSnapshots, error) -> {
                if (error != null) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("Wishlist", "Error loading wishlist: " + error.getMessage());
                    Toast.makeText(this, "Failed to load wishlist", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    wishlistWatches.clear();
                    List<String> watchIds = new ArrayList<>();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String watchId = document.getString("watchId");
                        if (watchId != null) {
                            watchIds.add(watchId);
                        }
                    }

                    if (watchIds.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        tvNoItems.setVisibility(View.VISIBLE);
                        tvWishlistCount.setText("Wishlist (0)");
                        wishlistAdapter.notifyDataSetChanged();
                        return;
                    }

                    loadWatchDetails(watchIds);
                }
            });
    }

    private void loadWatchDetails(List<String> watchIds) {
        wishlistWatches.clear();
        final int[] loadedCount = {0};

        for (String watchId : watchIds) {
            db.collection("watches").document(watchId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    loadedCount[0]++;
                    
                    if (documentSnapshot.exists()) {
                        Watch watch = documentSnapshot.toObject(Watch.class);
                        if (watch != null) {
                            watch.setId(documentSnapshot.getId());
                            wishlistWatches.add(watch);
                        }
                    }

                    if (loadedCount[0] == watchIds.size()) {
                        progressBar.setVisibility(View.GONE);
                        
                        if (wishlistWatches.isEmpty()) {
                            tvNoItems.setVisibility(View.VISIBLE);
                        } else {
                            tvNoItems.setVisibility(View.GONE);
                        }
                        
                        tvWishlistCount.setText("Wishlist (" + wishlistWatches.size() + ")");
                        wishlistAdapter.notifyDataSetChanged();
                    }
                })
                .addOnFailureListener(e -> {
                    loadedCount[0]++;
                    Log.e("Wishlist", "Error loading watch: " + e.getMessage());
                    
                    if (loadedCount[0] == watchIds.size()) {
                        progressBar.setVisibility(View.GONE);
                        tvWishlistCount.setText("Wishlist (" + wishlistWatches.size() + ")");
                        wishlistAdapter.notifyDataSetChanged();
                    }
                });
        }
    }

    private void removeFromWishlist(String wishlistItemId) {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        db.collection("wishlist")
            .whereEqualTo("userId", userId)
            .whereEqualTo("watchId", wishlistItemId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    document.getReference().delete()
                        .addOnSuccessListener(aVoid -> 
                            Toast.makeText(this, "Removed from wishlist", Toast.LENGTH_SHORT).show()
                        )
                        .addOnFailureListener(e -> 
                            Toast.makeText(this, "Failed to remove", Toast.LENGTH_SHORT).show()
                        );
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
