package com.example.watchstore_android_114;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchstore_android_114.models.Watch;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminDashboardActivity extends AppCompatActivity {

    private RecyclerView rvAdminWatches;
    private AdminWatchAdapter adminWatchAdapter;
    private List<Watch> watchList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoWatches, tvAdminName, tvTotalWatches, tvTotalUsers, tvTotalOrders, tvTotalRevenue;
    private Button btnAddWatch, btnLogout;

    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_dashboard);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);
        
        // Test Firestore connection
        Log.d("AdminDashboard", "Testing Firestore connection...");
        db.collection("test_connection")
            .document("test")
            .set(new HashMap<String, Object>() {{ put("timestamp", System.currentTimeMillis()); }})
            .addOnSuccessListener(aVoid -> {
                Log.d("AdminDashboard", "✅ Firestore connection successful!");
                Toast.makeText(this, "✅ Firestore connected", Toast.LENGTH_SHORT).show();
            })
            .addOnFailureListener(e -> {
                Log.e("AdminDashboard", "❌ Firestore connection failed: " + e.getMessage(), e);
                Toast.makeText(this, "❌ Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            });

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            navigateToLogin();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadStatistics();
        loadWatches();
    }

    private void initializeViews() {
        rvAdminWatches = findViewById(R.id.rv_admin_watches);
        progressBar = findViewById(R.id.progress_bar);
        tvNoWatches = findViewById(R.id.tv_no_watches_admin);
        tvAdminName = findViewById(R.id.tv_admin_name);
        tvTotalWatches = findViewById(R.id.tv_total_watches);
        tvTotalUsers = findViewById(R.id.tv_total_users);
        tvTotalOrders = findViewById(R.id.tv_total_orders);
        tvTotalRevenue = findViewById(R.id.tv_total_revenue);
        btnAddWatch = findViewById(R.id.btn_add_watch);
        btnLogout = findViewById(R.id.btn_logout);

        String username = sessionManager.getUsername();
        if (username != null) {
            tvAdminName.setText("Admin: " + username);
        }

        btnAddWatch.setOnClickListener(v -> showAddWatchDialog());
        btnLogout.setOnClickListener(v -> logout());
    }

    private void setupRecyclerView() {
        adminWatchAdapter = new AdminWatchAdapter(this, watchList, new AdminWatchAdapter.OnAdminWatchActionListener() {
            @Override
            public void onEditWatch(Watch watch) {
                showEditWatchDialog(watch);
            }

            @Override
            public void onDeleteWatch(Watch watch) {
                confirmDeleteWatch(watch);
            }
        });

        rvAdminWatches.setLayoutManager(new LinearLayoutManager(this));
        rvAdminWatches.setAdapter(adminWatchAdapter);
    }

    private void loadStatistics() {
        db.collection("watches").get().addOnSuccessListener(queryDocumentSnapshots -> 
            tvTotalWatches.setText(String.valueOf(queryDocumentSnapshots.size()))
        );

        db.collection("users").get().addOnSuccessListener(queryDocumentSnapshots -> 
            tvTotalUsers.setText(String.valueOf(queryDocumentSnapshots.size()))
        );

        db.collection("orders").get().addOnSuccessListener(queryDocumentSnapshots -> {
            int totalOrders = queryDocumentSnapshots.size();
            double totalRevenue = 0;
            
            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                Double amount = document.getDouble("totalAmount");
                if (amount != null) {
                    totalRevenue += amount;
                }
            }
            
            tvTotalOrders.setText(String.valueOf(totalOrders));
            tvTotalRevenue.setText(String.format("$%.2f", totalRevenue));
        });
    }

    private void loadWatches() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoWatches.setVisibility(View.GONE);

        // Use realtime listener for automatic updates
        db.collection("watches")
            .orderBy("name")
            .addSnapshotListener((queryDocumentSnapshots, error) -> {
                if (error != null) {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Failed to load watches: " + error.getMessage(),
                        Toast.LENGTH_SHORT).show();
                    return;
                }
                
                if (queryDocumentSnapshots != null) {
                    watchList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Watch watch = document.toObject(Watch.class);
                        watch.setId(document.getId());
                        watchList.add(watch);
                    }
                    
                    adminWatchAdapter.notifyDataSetChanged();
                    progressBar.setVisibility(View.GONE);
                    
                    if (watchList.isEmpty()) {
                        tvNoWatches.setVisibility(View.VISIBLE);
                    } else {
                        tvNoWatches.setVisibility(View.GONE);
                    }
                    
                    tvTotalWatches.setText(String.valueOf(watchList.size()));
                }
            });
    }

    private void showAddWatchDialog() {
        Log.d("AdminDashboard", "showAddWatchDialog called");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_watch, null);
        
        TextInputEditText etName = dialogView.findViewById(R.id.et_watch_name);
        TextInputEditText etBrand = dialogView.findViewById(R.id.et_watch_brand);
        TextInputEditText etPrice = dialogView.findViewById(R.id.et_watch_price);
        TextInputEditText etDescription = dialogView.findViewById(R.id.et_watch_description);
        TextInputEditText etStock = dialogView.findViewById(R.id.et_watch_stock);
        TextInputEditText etCategory = dialogView.findViewById(R.id.et_watch_category);
        TextInputEditText etImageUrl = dialogView.findViewById(R.id.et_watch_image_url);

        builder.setView(dialogView)
            .setTitle("Add New Watch")
            .setPositiveButton("Add", null)
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();
        
        Toast.makeText(this, "Add Watch Dialog Opened", Toast.LENGTH_SHORT).show();
        Log.d("AdminDashboard", "Dialog shown");

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Toast.makeText(this, "Add button clicked!", Toast.LENGTH_SHORT).show();
            Log.d("AdminDashboard", "Add button clicked");
            
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            String brand = etBrand.getText() != null ? etBrand.getText().toString().trim() : "";
            String priceStr = etPrice.getText() != null ? etPrice.getText().toString().trim() : "";
            String description = etDescription.getText() != null ? etDescription.getText().toString().trim() : "";
            String stockStr = etStock.getText() != null ? etStock.getText().toString().trim() : "";
            String category = etCategory.getText() != null ? etCategory.getText().toString().trim() : "";
            String imageUrl = etImageUrl.getText() != null ? etImageUrl.getText().toString().trim() : "";

            Log.d("AdminDashboard", "Form data - Name: " + name + ", Brand: " + brand + 
                  ", Price: " + priceStr + ", Stock: " + stockStr + ", Category: " + category);

            // Detailed validation
            if (name.isEmpty()) {
                Toast.makeText(this, "❌ Please enter watch name", Toast.LENGTH_LONG).show();
                return;
            }
            if (brand.isEmpty()) {
                Toast.makeText(this, "❌ Please enter brand", Toast.LENGTH_LONG).show();
                return;
            }
            if (priceStr.isEmpty()) {
                Toast.makeText(this, "❌ Please enter price", Toast.LENGTH_LONG).show();
                return;
            }
            if (stockStr.isEmpty()) {
                Toast.makeText(this, "❌ Please enter stock quantity", Toast.LENGTH_LONG).show();
                return;
            }
            if (category.isEmpty()) {
                Toast.makeText(this, "❌ Please enter category (Luxury/Sport/Classic/Smart)", Toast.LENGTH_LONG).show();
                return;
            }

            Log.d("AdminDashboard", "Validation passed, parsing numbers");

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                
                Log.d("AdminDashboard", "Parsed - Price: " + price + ", Stock: " + stock);

                Map<String, Object> watchData = new HashMap<>();
                watchData.put("name", name);
                watchData.put("brand", brand);
                watchData.put("price", price);
                watchData.put("description", description);
                watchData.put("stock", stock);
                watchData.put("category", category);
                watchData.put("imageUrl", imageUrl.isEmpty() ? null : imageUrl);
                watchData.put("createdAt", System.currentTimeMillis());

                Log.d("AdminDashboard", "Attempting to save to Firestore: " + watchData.toString());
                Log.d("AdminDashboard", "Firestore instance: " + (db != null ? "initialized" : "NULL"));

                if (db == null) {
                    Toast.makeText(this, "❌ Database not initialized!", Toast.LENGTH_LONG).show();
                    return;
                }

                db.collection("watches")
                    .add(watchData)
                    .addOnSuccessListener(documentReference -> {
                        Log.d("AdminDashboard", "✅ SUCCESS! Watch added with ID: " + documentReference.getId());
                        Toast.makeText(this, "✅ Watch added successfully!\n" + name, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        // Realtime listener will automatically update the list
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminDashboard", "❌ FAILED to add watch: " + e.getMessage(), e);
                        Toast.makeText(this, "❌ Failed to add watch:\n" + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
            } catch (NumberFormatException e) {
                Log.e("AdminDashboard", "Number parse error: " + e.getMessage());
                Toast.makeText(this, "❌ Invalid number format!\nPrice and Stock must be numbers", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showEditWatchDialog(Watch watch) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_watch, null);
        
        TextInputEditText etName = dialogView.findViewById(R.id.et_watch_name);
        TextInputEditText etBrand = dialogView.findViewById(R.id.et_watch_brand);
        TextInputEditText etPrice = dialogView.findViewById(R.id.et_watch_price);
        TextInputEditText etDescription = dialogView.findViewById(R.id.et_watch_description);
        TextInputEditText etStock = dialogView.findViewById(R.id.et_watch_stock);
        TextInputEditText etCategory = dialogView.findViewById(R.id.et_watch_category);
        TextInputEditText etImageUrl = dialogView.findViewById(R.id.et_watch_image_url);

        etName.setText(watch.getName());
        etBrand.setText(watch.getBrand());
        etPrice.setText(String.valueOf(watch.getPrice()));
        etDescription.setText(watch.getDescription());
        etStock.setText(String.valueOf(watch.getStock()));
        etCategory.setText(watch.getCategory());
        etImageUrl.setText(watch.getImageUrl());

        builder.setView(dialogView)
            .setTitle("Edit Watch")
            .setPositiveButton("Update", null)
            .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            Log.d("AdminDashboard", "Update button clicked for watch ID: " + watch.getId());
            
            String name = etName.getText().toString().trim();
            String brand = etBrand.getText().toString().trim();
            String priceStr = etPrice.getText().toString().trim();
            String description = etDescription.getText().toString().trim();
            String stockStr = etStock.getText().toString().trim();
            String category = etCategory.getText().toString().trim();
            String imageUrl = etImageUrl.getText().toString().trim();

            // Detailed validation
            if (name.isEmpty()) {
                Toast.makeText(this, "❌ Please enter watch name", Toast.LENGTH_LONG).show();
                return;
            }
            if (brand.isEmpty()) {
                Toast.makeText(this, "❌ Please enter brand", Toast.LENGTH_LONG).show();
                return;
            }
            if (priceStr.isEmpty()) {
                Toast.makeText(this, "❌ Please enter price", Toast.LENGTH_LONG).show();
                return;
            }
            if (stockStr.isEmpty()) {
                Toast.makeText(this, "❌ Please enter stock quantity", Toast.LENGTH_LONG).show();
                return;
            }
            if (category.isEmpty()) {
                Toast.makeText(this, "❌ Please enter category (Luxury/Sport/Classic/Smart)", Toast.LENGTH_LONG).show();
                return;
            }

            try {
                double price = Double.parseDouble(priceStr);
                int stock = Integer.parseInt(stockStr);
                
                Log.d("AdminDashboard", "Updating watch - Price: " + price + ", Stock: " + stock);

                Map<String, Object> watchData = new HashMap<>();
                watchData.put("name", name);
                watchData.put("brand", brand);
                watchData.put("price", price);
                watchData.put("description", description);
                watchData.put("stock", stock);
                watchData.put("category", category);
                watchData.put("imageUrl", imageUrl.isEmpty() ? null : imageUrl);

                db.collection("watches").document(watch.getId())
                    .update(watchData)
                    .addOnSuccessListener(aVoid -> {
                        Log.d("AdminDashboard", "✅ Watch updated successfully: " + watch.getId());
                        Toast.makeText(this, "✅ Watch updated successfully!\n" + name, Toast.LENGTH_LONG).show();
                        dialog.dismiss();
                        // Realtime listener will automatically update the list
                    })
                    .addOnFailureListener(e -> {
                        Log.e("AdminDashboard", "❌ Failed to update watch: " + e.getMessage(), e);
                        Toast.makeText(this, "❌ Failed to update watch:\n" + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
            } catch (NumberFormatException e) {
                Log.e("AdminDashboard", "Number parse error in update: " + e.getMessage());
                Toast.makeText(this, "❌ Invalid number format!\nPrice and Stock must be numbers", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void confirmDeleteWatch(Watch watch) {
        new AlertDialog.Builder(this)
            .setTitle("Delete Watch")
            .setMessage("Are you sure you want to delete " + watch.getName() + "?")
            .setPositiveButton("Delete", (dialog, which) -> deleteWatch(watch))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteWatch(Watch watch) {
        Log.d("AdminDashboard", "Deleting watch: " + watch.getName() + " (ID: " + watch.getId() + ")");
        
        db.collection("watches").document(watch.getId())
            .delete()
            .addOnSuccessListener(aVoid -> {
                Log.d("AdminDashboard", "✅ Watch deleted successfully: " + watch.getId());
                Toast.makeText(this, "✅ Watch deleted successfully!\n" + watch.getName(), Toast.LENGTH_LONG).show();
                // Realtime listener will automatically update the list
            })
            .addOnFailureListener(e -> {
                Log.e("AdminDashboard", "❌ Failed to delete watch: " + e.getMessage(), e);
                Toast.makeText(this, "❌ Failed to delete watch:\n" + e.getMessage(), 
                    Toast.LENGTH_LONG).show();
            });
    }

    private void logout() {
        sessionManager.logout();
        navigateToLogin();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
