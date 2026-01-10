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

import com.example.watchstore_android_114.models.CartItem;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CartActivity extends AppCompatActivity {

    private RecyclerView rvCart;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private ProgressBar progressBar;
    private TextView tvNoItems, tvSubtotal, tvTax, tvTotal, tvItemCount;
    private Button btnCheckout, btnContinueShopping;
    private View layoutCartSummary;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private static final double TAX_RATE = 0.08;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initializeViews();
        setupRecyclerView();
        loadCartItems();
    }

    private void initializeViews() {
        rvCart = findViewById(R.id.rv_cart);
        progressBar = findViewById(R.id.progress_bar);
        tvNoItems = findViewById(R.id.tv_no_items);
        tvSubtotal = findViewById(R.id.tv_subtotal);
        tvTax = findViewById(R.id.tv_tax);
        tvTotal = findViewById(R.id.tv_total);
        tvItemCount = findViewById(R.id.tv_item_count);
        btnCheckout = findViewById(R.id.btn_checkout);
        btnContinueShopping = findViewById(R.id.btn_continue_shopping);
        layoutCartSummary = findViewById(R.id.layout_cart_summary);

        btnCheckout.setOnClickListener(v -> proceedToCheckout());
        btnContinueShopping.setOnClickListener(v -> finish());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("My Cart");
        }
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this, cartItems, new CartAdapter.OnCartActionListener() {
            @Override
            public void onIncreaseQuantity(CartItem item) {
                updateCartItemQuantity(item, item.getQuantity() + 1);
            }

            @Override
            public void onDecreaseQuantity(CartItem item) {
                if (item.getQuantity() > 1) {
                    updateCartItemQuantity(item, item.getQuantity() - 1);
                }
            }

            @Override
            public void onRemoveItem(CartItem item) {
                showRemoveConfirmation(item);
            }
        });

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvCart.setAdapter(cartAdapter);
    }

    private void loadCartItems() {
        progressBar.setVisibility(View.VISIBLE);
        tvNoItems.setVisibility(View.GONE);
        layoutCartSummary.setVisibility(View.GONE);

        String userId = sessionManager.getUserId();
        
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItems.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CartItem item = document.toObject(CartItem.class);
                        item.setId(document.getId().hashCode());
                        cartItems.add(item);
                    }

                    progressBar.setVisibility(View.GONE);
                    
                    if (cartItems.isEmpty()) {
                        tvNoItems.setVisibility(View.VISIBLE);
                        layoutCartSummary.setVisibility(View.GONE);
                        rvCart.setVisibility(View.GONE);
                    } else {
                        tvNoItems.setVisibility(View.GONE);
                        layoutCartSummary.setVisibility(View.VISIBLE);
                        rvCart.setVisibility(View.VISIBLE);
                        cartAdapter.notifyDataSetChanged();
                        updateCartSummary();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("CartActivity", "Error loading cart: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load cart items", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateCartItemQuantity(CartItem item, int newQuantity) {
        if (newQuantity > item.getAvailableStock()) {
            Toast.makeText(this, "Cannot exceed available stock", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);
        
        String userId = sessionManager.getUserId();
        
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .whereEqualTo("watchId", item.getWatchId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        
                        db.collection("cart").document(docId)
                                .update("quantity", newQuantity)
                                .addOnSuccessListener(aVoid -> {
                                    item.setQuantity(newQuantity);
                                    cartAdapter.notifyDataSetChanged();
                                    updateCartSummary();
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Quantity updated", Toast.LENGTH_SHORT).show();
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e("CartActivity", "Error updating quantity: " + e.getMessage());
                                    Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("CartActivity", "Error finding cart item: " + e.getMessage());
                });
    }

    private void showRemoveConfirmation(CartItem item) {
        new AlertDialog.Builder(this)
                .setTitle("Remove Item")
                .setMessage("Remove " + item.getWatchName() + " from cart?")
                .setPositiveButton("Remove", (dialog, which) -> removeCartItem(item))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void removeCartItem(CartItem item) {
        progressBar.setVisibility(View.VISIBLE);
        
        String userId = sessionManager.getUserId();
        
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .whereEqualTo("watchId", item.getWatchId())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        String docId = queryDocumentSnapshots.getDocuments().get(0).getId();
                        
                        db.collection("cart").document(docId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    cartItems.remove(item);
                                    cartAdapter.notifyDataSetChanged();
                                    updateCartSummary();
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this, "Item removed from cart", Toast.LENGTH_SHORT).show();
                                    
                                    if (cartItems.isEmpty()) {
                                        tvNoItems.setVisibility(View.VISIBLE);
                                        layoutCartSummary.setVisibility(View.GONE);
                                        rvCart.setVisibility(View.GONE);
                                    }
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Log.e("CartActivity", "Error removing item: " + e.getMessage());
                                    Toast.makeText(this, "Failed to remove item", Toast.LENGTH_SHORT).show();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("CartActivity", "Error finding cart item: " + e.getMessage());
                });
    }

    private void updateCartSummary() {
        double subtotal = 0;
        int itemCount = 0;
        
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
            itemCount += item.getQuantity();
        }
        
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;
        
        tvSubtotal.setText(String.format("$%.2f", subtotal));
        tvTax.setText(String.format("$%.2f", tax));
        tvTotal.setText(String.format("$%.2f", total));
        tvItemCount.setText(itemCount + " item(s)");
    }

    private void proceedToCheckout() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, CheckoutActivity.class);
        startActivity(intent);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCartItems();
    }
}
