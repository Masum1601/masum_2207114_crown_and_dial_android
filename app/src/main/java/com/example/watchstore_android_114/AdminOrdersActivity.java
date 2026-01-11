package com.example.watchstore_android_114;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.watchstore_android_114.models.Order;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AdminOrdersActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private AdminOrdersAdapter adminOrdersAdapter;
    private List<Order> orderList = new ArrayList<>();
    private List<Order> filteredOrderList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoOrders, tvOrderCount;
    private Spinner spinnerStatusFilter;
    private Button btnBack;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private String currentFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_orders);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn() || !sessionManager.isAdmin()) {
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        setupFilterSpinner();
        loadOrders();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Order Management");
        }
    }

    private void initializeViews() {
        rvOrders = findViewById(R.id.rv_admin_orders);
        progressBar = findViewById(R.id.progress_bar);
        tvNoOrders = findViewById(R.id.tv_no_orders);
        tvOrderCount = findViewById(R.id.tv_order_count);
        spinnerStatusFilter = findViewById(R.id.spinner_status_filter);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        adminOrdersAdapter = new AdminOrdersAdapter(this, filteredOrderList, order -> {
            showUpdateStatusDialog(order);
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(adminOrdersAdapter);
    }

    private void setupFilterSpinner() {
        String[] filters = {"All", "Pending", "Processing", "Shipped", "Delivered", "Cancelled"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, filters);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatusFilter.setAdapter(adapter);

        spinnerStatusFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = filters[position];
                filterOrders();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void loadOrders() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoOrders.setVisibility(View.GONE);

        db.collection("orders")
            .addSnapshotListener((queryDocumentSnapshots, error) -> {
                if (error != null) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("AdminOrders", "Error loading orders: " + error.getMessage());
                    Toast.makeText(this, "Failed to load orders", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (queryDocumentSnapshots != null) {
                    orderList.clear();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            Order order = new Order();
                            order.setDocumentId(document.getId());
                            order.setId(document.getId().hashCode());
                            
                            String userIdStr = document.getString("userId");
                            if (userIdStr != null) {
                                order.setUserId(0);
                            }
                            
                            order.setUsername(document.getString("userName"));
                            
                            Double totalAmount = document.getDouble("totalAmount");
                            if (totalAmount != null) {
                                order.setTotalAmount(totalAmount);
                            }
                            
                            order.setStatus(document.getString("status"));
                            
                            Long orderDate = document.getLong("orderDate");
                            if (orderDate != null) {
                                order.setOrderDate(orderDate);
                            }
                            
                            List<Object> itemsData = (List<Object>) document.get("items");
                            if (itemsData != null) {
                                List<Order.OrderItem> items = new ArrayList<>();
                                for (Object itemObj : itemsData) {
                                    if (itemObj instanceof Map) {
                                        Map<String, Object> itemMap = (Map<String, Object>) itemObj;
                                        Order.OrderItem item = new Order.OrderItem();
                                        item.setWatchId((String) itemMap.get("watchId"));
                                        item.setWatchName((String) itemMap.get("watchName"));
                                        
                                        Object qtyObj = itemMap.get("quantity");
                                        if (qtyObj instanceof Long) {
                                            item.setQuantity(((Long) qtyObj).intValue());
                                        } else if (qtyObj instanceof Integer) {
                                            item.setQuantity((Integer) qtyObj);
                                        }
                                        
                                        Object priceObj = itemMap.get("price");
                                        if (priceObj instanceof Double) {
                                            item.setPrice((Double) priceObj);
                                        } else if (priceObj instanceof Long) {
                                            item.setPrice(((Long) priceObj).doubleValue());
                                        }
                                        
                                        items.add(item);
                                    }
                                }
                                order.setItems(items);
                            }
                            
                            orderList.add(order);
                        } catch (Exception e) {
                            Log.e("AdminOrders", "Error parsing order: " + e.getMessage(), e);
                        }
                    }

                    // Sort by date descending
                    orderList.sort((o1, o2) -> Long.compare(o2.getOrderDate(), o1.getOrderDate()));

                    progressBar.setVisibility(View.GONE);
                    filterOrders();
                }
            });
    }

    private void filterOrders() {
        filteredOrderList.clear();

        if (currentFilter.equals("All")) {
            filteredOrderList.addAll(orderList);
        } else {
            for (Order order : orderList) {
                if (order.getStatus() != null && order.getStatus().equals(currentFilter)) {
                    filteredOrderList.add(order);
                }
            }
        }

        if (filteredOrderList.isEmpty()) {
            tvNoOrders.setVisibility(View.VISIBLE);
        } else {
            tvNoOrders.setVisibility(View.GONE);
        }

        tvOrderCount.setText("Orders (" + filteredOrderList.size() + ")");
        adminOrdersAdapter.notifyDataSetChanged();
    }

    private void showUpdateStatusDialog(Order order) {
        String[] statuses = {"Pending", "Processing", "Shipped", "Delivered", "Cancelled"};
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Order Status");
        builder.setItems(statuses, (dialog, which) -> {
            String newStatus = statuses[which];
            updateOrderStatus(order, newStatus);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateOrderStatus(Order order, String newStatus) {
        if (order.getDocumentId() == null) {
            Toast.makeText(this, "Order ID not found", Toast.LENGTH_SHORT).show();
            return;
        }
        
        Map<String, Object> updates = new HashMap<>();
        updates.put("status", newStatus);
        
        db.collection("orders")
            .document(order.getDocumentId())
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Order status updated to " + newStatus, 
                    Toast.LENGTH_SHORT).show();
                order.setStatus(newStatus);
                adminOrdersAdapter.notifyDataSetChanged();
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Failed to update status: " + e.getMessage(), 
                    Toast.LENGTH_SHORT).show();
                Log.e("AdminOrders", "Error updating status: " + e.getMessage());
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
