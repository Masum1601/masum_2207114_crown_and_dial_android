package com.example.watchstore_android_114;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchstore_android_114.models.Watch;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.android.material.chip.Chip;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UserDashboardActivity extends AppCompatActivity {

    private RecyclerView rvWatches;
    private WatchAdapter watchAdapter;
    private List<Watch> watchList = new ArrayList<>();
    private List<Watch> filteredWatchList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoWatches, tvUserName;
    private TextInputEditText etSearch;
    private Spinner spinnerSort;
    private Button btnCart, btnWishlist, btnLogout;
    private Chip chipAll, chipLuxury, chipSport, chipClassic, chipSmart;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String currentCategory = "ALL";
    private int cartItemCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_dashboard);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupCategoryFilters();
        setupSortSpinner();
        setupSearch();
        setupButtons();
        
        loadWatches();
        updateCartCount();
    }

    private void initializeViews() {
        rvWatches = findViewById(R.id.rv_watches);
        progressBar = findViewById(R.id.progress_bar);
        tvNoWatches = findViewById(R.id.tv_no_watches);
        tvUserName = findViewById(R.id.tv_user_name);
        etSearch = findViewById(R.id.et_search);
        spinnerSort = findViewById(R.id.spinner_sort);
        btnCart = findViewById(R.id.btn_cart);
        btnWishlist = findViewById(R.id.btn_wishlist);
        btnLogout = findViewById(R.id.btn_logout);
        
        chipAll = findViewById(R.id.chip_all);
        chipLuxury = findViewById(R.id.chip_luxury);
        chipSport = findViewById(R.id.chip_sport);
        chipClassic = findViewById(R.id.chip_classic);
        chipSmart = findViewById(R.id.chip_smart);

        String username = sessionManager.getUsername();
        if (username != null) {
            tvUserName.setText("Welcome, " + username);
        }
    }

    private void setupRecyclerView() {
        watchAdapter = new WatchAdapter(this, filteredWatchList, new WatchAdapter.OnWatchActionListener() {
            @Override
            public void onAddToCart(Watch watch) {
                addToCart(watch);
            }

            @Override
            public void onAddToWishlist(Watch watch) {
                addToWishlist(watch);
            }

            @Override
            public void onViewDetails(Watch watch) {
                viewWatchDetails(watch);
            }
        });

        rvWatches.setLayoutManager(new GridLayoutManager(this, 2));
        rvWatches.setAdapter(watchAdapter);
    }

    private void setupCategoryFilters() {
        chipAll.setOnClickListener(v -> filterByCategory("ALL"));
        chipLuxury.setOnClickListener(v -> filterByCategory("Luxury"));
        chipSport.setOnClickListener(v -> filterByCategory("Sport"));
        chipClassic.setOnClickListener(v -> filterByCategory("Classic"));
        chipSmart.setOnClickListener(v -> filterByCategory("Smart"));
    }

    private void setupSortSpinner() {
        String[] sortOptions = {
            "Default",
            "Price: Low to High",
            "Price: High to Low",
            "Name: A-Z",
            "Brand: A-Z"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
            this,
            android.R.layout.simple_spinner_item,
            sortOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);

        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                sortWatches(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterWatches(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void setupButtons() {
        btnCart.setOnClickListener(v -> {
            Toast.makeText(this, "Cart feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnWishlist.setOnClickListener(v -> {
            Toast.makeText(this, "Wishlist feature coming soon!", Toast.LENGTH_SHORT).show();
        });

        btnLogout.setOnClickListener(v -> logout());
    }

    private void loadWatches() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoWatches.setVisibility(View.GONE);

        Query query = db.collection("watches");

        if (!currentCategory.equals("ALL")) {
            query = query.whereEqualTo("category", currentCategory);
        }

        query.whereGreaterThan("stock", 0)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                watchList.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Watch watch = document.toObject(Watch.class);
                    watch.setId(document.getId());
                    watchList.add(watch);
                }
                
                filterWatches(etSearch.getText().toString());
                progressBar.setVisibility(View.GONE);
                
                if (watchList.isEmpty()) {
                    tvNoWatches.setVisibility(View.VISIBLE);
                }
            })
            .addOnFailureListener(e -> {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(this, "Failed to load watches: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
            });
    }

    private void filterByCategory(String category) {
        currentCategory = category;
        loadWatches();
    }

    private void filterWatches(String searchText) {
        filteredWatchList.clear();
        
        if (searchText.isEmpty()) {
            filteredWatchList.addAll(watchList);
        } else {
            String lowerCaseSearch = searchText.toLowerCase();
            for (Watch watch : watchList) {
                if (watch.getName().toLowerCase().contains(lowerCaseSearch) ||
                    watch.getBrand().toLowerCase().contains(lowerCaseSearch) ||
                    (watch.getDescription() != null && 
                     watch.getDescription().toLowerCase().contains(lowerCaseSearch))) {
                    filteredWatchList.add(watch);
                }
            }
        }
        
        watchAdapter.notifyDataSetChanged();
        tvNoWatches.setVisibility(filteredWatchList.isEmpty() ? View.VISIBLE : View.GONE);
    }

    private void sortWatches(int sortType) {
        switch (sortType) {
            case 1:
                filteredWatchList.sort((w1, w2) -> Double.compare(w1.getPrice(), w2.getPrice()));
                break;
            case 2:
                filteredWatchList.sort((w1, w2) -> Double.compare(w2.getPrice(), w1.getPrice()));
                break;
            case 3:
                filteredWatchList.sort((w1, w2) -> w1.getName().compareToIgnoreCase(w2.getName()));
                break;
            case 4:
                filteredWatchList.sort((w1, w2) -> w1.getBrand().compareToIgnoreCase(w2.getBrand()));
                break;
        }
        watchAdapter.notifyDataSetChanged();
    }

    private void addToCart(Watch watch) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("cart")
            .whereEqualTo("userId", userId)
            .whereEqualTo("watchId", watch.getId())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(this, "Already in cart", Toast.LENGTH_SHORT).show();
                } else {
                    addNewCartItem(userId, watch);
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
            );
    }

    private void addNewCartItem(String userId, Watch watch) {
        java.util.Map<String, Object> cartItem = new java.util.HashMap<>();
        cartItem.put("userId", userId);
        cartItem.put("watchId", watch.getId());
        cartItem.put("watchName", watch.getName());
        cartItem.put("watchPrice", watch.getPrice());
        cartItem.put("quantity", 1);
        cartItem.put("addedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("cart")
            .add(cartItem)
            .addOnSuccessListener(documentReference -> {
                Toast.makeText(this, watch.getName() + " added to cart!", 
                    Toast.LENGTH_SHORT).show();
                updateCartCount();
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to add to cart", Toast.LENGTH_SHORT).show()
            );
    }

    private void addToWishlist(Watch watch) {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("wishlist")
            .whereEqualTo("userId", userId)
            .whereEqualTo("watchId", watch.getId())
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    Toast.makeText(this, "Already in wishlist", Toast.LENGTH_SHORT).show();
                } else {
                    addNewWishlistItem(userId, watch);
                }
            })
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to add to wishlist", Toast.LENGTH_SHORT).show()
            );
    }

    private void addNewWishlistItem(String userId, Watch watch) {
        java.util.Map<String, Object> wishlistItem = new java.util.HashMap<>();
        wishlistItem.put("userId", userId);
        wishlistItem.put("watchId", watch.getId());
        wishlistItem.put("watchName", watch.getName());
        wishlistItem.put("watchBrand", watch.getBrand());
        wishlistItem.put("watchPrice", watch.getPrice());
        wishlistItem.put("addedAt", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("wishlist")
            .add(wishlistItem)
            .addOnSuccessListener(documentReference -> 
                Toast.makeText(this, watch.getName() + " added to wishlist!", 
                    Toast.LENGTH_SHORT).show()
            )
            .addOnFailureListener(e -> 
                Toast.makeText(this, "Failed to add to wishlist", Toast.LENGTH_SHORT).show()
            );
    }

    private void viewWatchDetails(Watch watch) {
        Toast.makeText(this, "Watch details: " + watch.getName(), Toast.LENGTH_SHORT).show();
    }

    private void updateCartCount() {
        String userId = sessionManager.getUserId();
        if (userId == null) return;

        db.collection("cart")
            .whereEqualTo("userId", userId)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                cartItemCount = queryDocumentSnapshots.size();
                btnCart.setText("🛒 Cart (" + cartItemCount + ")");
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

    @Override
    protected void onResume() {
        super.onResume();
        updateCartCount();
    }
}
