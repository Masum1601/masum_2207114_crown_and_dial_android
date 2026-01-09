package com.example.watchstore_android_114.examples;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.watchstore_android_114.R;
import com.example.watchstore_android_114.models.Watch;
import com.example.watchstore_android_114.utils.WatchFirebaseManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WatchManagementExampleActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private EditText etSearchBrand;
    private Button btnLoadAll, btnSearch, btnAddSample;
    
    private WatchFirebaseManager watchManager;
    private List<Watch> watchList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        watchManager = WatchFirebaseManager.getInstance();
        watchList = new ArrayList<>();
        
        
        // progressBar = findViewById(R.id.progressBar);
        // etSearchBrand = findViewById(R.id.etSearchBrand);
        // btnLoadAll = findViewById(R.id.btnLoadAll);
        // btnSearch = findViewById(R.id.btnSearch);
        // btnAddSample = findViewById(R.id.btnAddSample);
        
        // Setup RecyclerView
        // recyclerView.setLayoutManager(new LinearLayoutManager(this));
        // adapter = new WatchAdapter(watchList, this::onWatchClick);
        // recyclerView.setAdapter(adapter);
        
        setupButtonListeners();
    }
    
    private void setupButtonListeners() {
    }
    
    private void loadAllWatches() {
        showLoading(true);
        
        watchManager.getAllWatches(new WatchFirebaseManager.WatchesCallback() {
            @Override
            public void onSuccess(List<Watch> watches) {
                showLoading(false);
                watchList.clear();
                watchList.addAll(watches);
                // adapter.notifyDataSetChanged();
                
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Loaded " + watches.size() + " watches", 
                    Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(String errorMessage) {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Error: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void searchWatchesByBrand(String brand) {
        showLoading(true);
        
        watchManager.getWatchesByBrand(brand, new WatchFirebaseManager.WatchesCallback() {
            @Override
            public void onSuccess(List<Watch> watches) {
                showLoading(false);
                watchList.clear();
                watchList.addAll(watches);
                
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Found " + watches.size() + " watches", 
                    Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(String errorMessage) {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Error: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void addSampleWatch() {
        Watch newWatch = new Watch();
        newWatch.setName("Rolex Submariner");
        newWatch.setBrand("Rolex");
        newWatch.setPrice(8500.00);
        newWatch.setDescription("Iconic luxury diving watch with automatic movement");
        newWatch.setStock(3);
        newWatch.setCategory("Luxury");
        newWatch.setImageUrl("https://example.com/rolex-submariner.jpg");
        
        showLoading(true);
        
        watchManager.addWatch(newWatch, new WatchFirebaseManager.WatchIdCallback() {
            @Override
            public void onSuccess(String watchId) {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Watch added successfully! ID: " + watchId, 
                    Toast.LENGTH_SHORT).show();
                
                loadAllWatches();
            }
            
            @Override
            public void onFailure(String errorMessage) {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Error adding watch: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void updateWatch(String watchId, int newStock, double newPrice) {
        Map<String, Object> updates = new HashMap<>();
        updates.put("stock", newStock);
        updates.put("price", newPrice);
        
        showLoading(true);
        
        watchManager.updateWatch(watchId, updates, new WatchFirebaseManager.SuccessCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Watch updated successfully", 
                    Toast.LENGTH_SHORT).show();
                
                loadAllWatches();
            }
            
            @Override
            public void onFailure(String errorMessage) {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Error updating watch: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void deleteWatch(String watchId) {
        showLoading(true);
        
        watchManager.deleteWatch(watchId, new WatchFirebaseManager.SuccessCallback() {
            @Override
            public void onSuccess() {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Watch deleted successfully", 
                    Toast.LENGTH_SHORT).show();
                
                loadAllWatches();
            }
            
            @Override
            public void onFailure(String errorMessage) {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Error deleting watch: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void searchByPriceRange(double minPrice, double maxPrice) {
        showLoading(true);
        
        watchManager.getWatchesByPriceRange(minPrice, maxPrice, 
            new WatchFirebaseManager.WatchesCallback() {
                @Override
                public void onSuccess(List<Watch> watches) {
                    showLoading(false);
                    watchList.clear();
                    watchList.addAll(watches);
                    
                    Toast.makeText(WatchManagementExampleActivity.this, 
                        "Found " + watches.size() + " watches in range", 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onFailure(String errorMessage) {
                    showLoading(false);
                    Toast.makeText(WatchManagementExampleActivity.this, 
                        "Error: " + errorMessage, 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
    
    private void loadWatchesSortedByPrice(boolean ascending) {
        showLoading(true);
        
        watchManager.getWatchesSortedByPrice(ascending, 
            new WatchFirebaseManager.WatchesCallback() {
                @Override
                public void onSuccess(List<Watch> watches) {
                    showLoading(false);
                    watchList.clear();
                    watchList.addAll(watches);
                    
                    String order = ascending ? "lowest to highest" : "highest to lowest";
                    Toast.makeText(WatchManagementExampleActivity.this, 
                        "Sorted by price: " + order, 
                        Toast.LENGTH_SHORT).show();
                }
                
                @Override
                public void onFailure(String errorMessage) {
                    showLoading(false);
                    Toast.makeText(WatchManagementExampleActivity.this, 
                        "Error: " + errorMessage, 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
    
    private void loadAvailableWatches() {
        showLoading(true);
        
        watchManager.getAvailableWatches(new WatchFirebaseManager.WatchesCallback() {
            @Override
            public void onSuccess(List<Watch> watches) {
                showLoading(false);
                watchList.clear();
                watchList.addAll(watches);
                // adapter.notifyDataSetChanged();
                
                Toast.makeText(WatchManagementExampleActivity.this, 
                    watches.size() + " watches in stock", 
                    Toast.LENGTH_SHORT).show();
            }
            
            @Override
            public void onFailure(String errorMessage) {
                showLoading(false);
                Toast.makeText(WatchManagementExampleActivity.this, 
                    "Error: " + errorMessage, 
                    Toast.LENGTH_LONG).show();
            }
        });
    }
    
    private void purchaseWatch(String watchId, int quantity) {
        showLoading(true);
        
        watchManager.decreaseWatchStock(watchId, quantity, 
            new WatchFirebaseManager.SuccessCallback() {
                @Override
                public void onSuccess() {
                    showLoading(false);
                    Toast.makeText(WatchManagementExampleActivity.this, 
                        "Purchase successful!", 
                        Toast.LENGTH_SHORT).show();
                    
                    loadAllWatches();
                }
                
                @Override
                public void onFailure(String errorMessage) {
                    showLoading(false);
                    Toast.makeText(WatchManagementExampleActivity.this, 
                        "Purchase failed: " + errorMessage, 
                        Toast.LENGTH_LONG).show();
                }
            });
    }
    
    private void onWatchClick(Watch watch) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(watch.getName())
            .setMessage("Price: $" + watch.getPrice() + "\nStock: " + watch.getStock())
            .setPositiveButton("Update", (dialog, which) -> {
            })
            .setNegativeButton("Delete", (dialog, which) -> {
            })
            .setNeutralButton("Cancel", null)
            .show();
    }
    
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
    }
}
