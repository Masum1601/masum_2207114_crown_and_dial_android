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

import com.example.watchstore_android_114.models.Order;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {

    private RecyclerView rvOrders;
    private OrderHistoryAdapter orderHistoryAdapter;
    private List<Order> orderList = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoOrders, tvOrderCount;
    private Button btnBack;

    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            finish();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadOrders();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Order History");
        }
    }

    private void initializeViews() {
        rvOrders = findViewById(R.id.rv_orders);
        progressBar = findViewById(R.id.progress_bar);
        tvNoOrders = findViewById(R.id.tv_no_orders);
        tvOrderCount = findViewById(R.id.tv_order_count);
        btnBack = findViewById(R.id.btn_back);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupRecyclerView() {
        orderHistoryAdapter = new OrderHistoryAdapter(this, orderList, order -> {
            
        });

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        rvOrders.setAdapter(orderHistoryAdapter);
    }

    private void loadOrders() {
        String userId = sessionManager.getUserId();
        if (userId == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        tvNoOrders.setVisibility(View.GONE);

        db.collection("orders")
            .whereEqualTo("userId", userId)
            .addSnapshotListener((queryDocumentSnapshots, error) -> {
                if (error != null) {
                    progressBar.setVisibility(View.GONE);
                    Log.e("OrderHistory", "Error loading orders: " + error.getMessage());
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
                                    if (itemObj instanceof java.util.Map) {
                                        java.util.Map<String, Object> itemMap = (java.util.Map<String, Object>) itemObj;
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
                            Log.e("OrderHistory", "Error parsing order: " + e.getMessage(), e);
                        }
                    }

                    // Sort by date descending
                    orderList.sort((o1, o2) -> Long.compare(o2.getOrderDate(), o1.getOrderDate()));

                    progressBar.setVisibility(View.GONE);
                    
                    if (orderList.isEmpty()) {
                        tvNoOrders.setVisibility(View.VISIBLE);
                        tvOrderCount.setText("Orders (0)");
                    } else {
                        tvNoOrders.setVisibility(View.GONE);
                        tvOrderCount.setText("Orders (" + orderList.size() + ")");
                    }
                    
                    orderHistoryAdapter.notifyDataSetChanged();
                }
            });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
