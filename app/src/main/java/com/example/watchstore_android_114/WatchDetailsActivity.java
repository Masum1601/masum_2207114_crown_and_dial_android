package com.example.watchstore_android_114;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watchstore_android_114.models.Watch;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WatchDetailsActivity extends AppCompatActivity {

    private ImageView ivWatchImage;
    private TextView tvWatchIcon, tvWatchName, tvWatchBrand, tvWatchPrice, tvWatchCategory;
    private TextView tvWatchStock, tvWatchDescription;
    private Button btnAddToCart, btnAddToWishlist, btnBack;
    private Chip chipInStock;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private Watch watch;
    private String watchId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_details);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        watchId = getIntent().getStringExtra("WATCH_ID");

        if (watchId == null || watchId.isEmpty()) {
            Toast.makeText(this, "Invalid watch", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        loadWatchDetails();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Watch Details");
        }
    }

    private void initializeViews() {
        ivWatchImage = findViewById(R.id.iv_watch_detail_image);
        tvWatchIcon = findViewById(R.id.tv_watch_detail_icon);
        tvWatchName = findViewById(R.id.tv_watch_detail_name);
        tvWatchBrand = findViewById(R.id.tv_watch_detail_brand);
        tvWatchPrice = findViewById(R.id.tv_watch_detail_price);
        tvWatchCategory = findViewById(R.id.tv_watch_detail_category);
        tvWatchStock = findViewById(R.id.tv_watch_detail_stock);
        tvWatchDescription = findViewById(R.id.tv_watch_detail_description);
        chipInStock = findViewById(R.id.chip_in_stock);
        btnAddToCart = findViewById(R.id.btn_detail_add_to_cart);
        btnAddToWishlist = findViewById(R.id.btn_detail_add_to_wishlist);
        btnBack = findViewById(R.id.btn_detail_back);
        progressBar = findViewById(R.id.progress_bar);

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadWatchDetails() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("watches").document(watchId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        watch = documentSnapshot.toObject(Watch.class);
                        if (watch != null) {
                            watch.setId(documentSnapshot.getId());
                            displayWatchDetails();
                        }
                    } else {
                        Toast.makeText(this, "Watch not found", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                    progressBar.setVisibility(View.GONE);
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("WatchDetails", "Error loading watch: " + e.getMessage());
                    Toast.makeText(this, "Failed to load watch details", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void displayWatchDetails() {
        tvWatchName.setText(watch.getName());
        tvWatchBrand.setText(watch.getBrand());
        tvWatchPrice.setText(String.format("$%.2f", watch.getPrice()));
        tvWatchCategory.setText(watch.getCategory() != null ? watch.getCategory() : "Uncategorized");
        
        if (watch.getDescription() != null && !watch.getDescription().isEmpty()) {
            tvWatchDescription.setText(watch.getDescription());
        } else {
            tvWatchDescription.setText("No description available.");
        }

        if (watch.getStock() > 0) {
            tvWatchStock.setText(watch.getStock() + " units available");
            tvWatchStock.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            chipInStock.setText("In Stock");
            chipInStock.setChipBackgroundColorResource(android.R.color.holo_green_light);
            btnAddToCart.setEnabled(true);
        } else {
            tvWatchStock.setText("Out of Stock");
            tvWatchStock.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            chipInStock.setText("Out of Stock");
            chipInStock.setChipBackgroundColorResource(android.R.color.holo_red_light);
            btnAddToCart.setEnabled(false);
        }

        if (watch.getImageUrl() != null && !watch.getImageUrl().isEmpty()) {
            ivWatchImage.setVisibility(View.VISIBLE);
            tvWatchIcon.setVisibility(View.GONE);
            
            // Load image with Glide
            com.bumptech.glide.Glide.with(this)
                .load(watch.getImageUrl())
                .placeholder(android.R.drawable.ic_menu_gallery)
                .error(android.R.drawable.ic_menu_report_image)
                .into(ivWatchImage);
        } else {
            ivWatchImage.setVisibility(View.GONE);
            tvWatchIcon.setVisibility(View.VISIBLE);
        }

        btnAddToCart.setOnClickListener(v -> addToCart());
        btnAddToWishlist.setOnClickListener(v -> addToWishlist());
    }

    private void addToCart() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("cart")
                .whereEqualTo("userId", userId)
                .whereEqualTo("watchId", watch.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Already in cart", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> cartItem = new HashMap<>();
                        cartItem.put("userId", userId);
                        cartItem.put("watchId", watch.getId());
                        cartItem.put("watchName", watch.getName());
                        cartItem.put("watchBrand", watch.getBrand());
                        cartItem.put("watchPrice", watch.getPrice());
                        cartItem.put("quantity", 1);
                        cartItem.put("availableStock", watch.getStock());
                        cartItem.put("addedAt", System.currentTimeMillis());

                        db.collection("cart")
                                .add(cartItem)
                                .addOnSuccessListener(documentReference -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, watch.getName() + " added to cart!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to check cart", Toast.LENGTH_SHORT).show();
                });
    }

    private void addToWishlist() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        db.collection("wishlist")
                .whereEqualTo("userId", userId)
                .whereEqualTo("watchId", watch.getId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Already in wishlist", Toast.LENGTH_SHORT).show();
                    } else {
                        Map<String, Object> wishlistItem = new HashMap<>();
                        wishlistItem.put("userId", userId);
                        wishlistItem.put("watchId", watch.getId());
                        wishlistItem.put("watchName", watch.getName());
                        wishlistItem.put("watchBrand", watch.getBrand());
                        wishlistItem.put("watchPrice", watch.getPrice());
                        wishlistItem.put("addedAt", System.currentTimeMillis());

                        db.collection("wishlist")
                                .add(wishlistItem)
                                .addOnSuccessListener(documentReference -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, watch.getName() + " added to wishlist!", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Failed to add to wishlist", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to check wishlist", Toast.LENGTH_SHORT).show();
                });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
