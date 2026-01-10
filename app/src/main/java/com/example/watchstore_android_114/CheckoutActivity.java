package com.example.watchstore_android_114;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watchstore_android_114.models.CartItem;
import com.example.watchstore_android_114.models.Order;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CheckoutActivity extends AppCompatActivity {

    private TextInputEditText etFullName, etEmail, etPhone, etAddress, etCity, etZipCode;
    private RadioGroup rgPaymentMethod;
    private TextView tvOrderSubtotal, tvOrderTax, tvOrderTotal, tvItemCount;
    private Button btnPlaceOrder, btnCancel;
    private ProgressBar progressBar;

    private FirebaseFirestore db;
    private SessionManager sessionManager;
    private List<CartItem> cartItems = new ArrayList<>();
    private static final double TAX_RATE = 0.08;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checkout);

        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initializeViews();
        loadCartItems();
        prefillUserInfo();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Checkout");
        }
    }

    private void initializeViews() {
        etFullName = findViewById(R.id.et_full_name);
        etEmail = findViewById(R.id.et_email);
        etPhone = findViewById(R.id.et_phone);
        etAddress = findViewById(R.id.et_address);
        etCity = findViewById(R.id.et_city);
        etZipCode = findViewById(R.id.et_zip_code);
        rgPaymentMethod = findViewById(R.id.rg_payment_method);
        tvOrderSubtotal = findViewById(R.id.tv_order_subtotal);
        tvOrderTax = findViewById(R.id.tv_order_tax);
        tvOrderTotal = findViewById(R.id.tv_order_total);
        tvItemCount = findViewById(R.id.tv_checkout_item_count);
        btnPlaceOrder = findViewById(R.id.btn_place_order);
        btnCancel = findViewById(R.id.btn_cancel_checkout);
        progressBar = findViewById(R.id.progress_bar);

        btnPlaceOrder.setOnClickListener(v -> validateAndPlaceOrder());
        btnCancel.setOnClickListener(v -> finish());
    }

    private void prefillUserInfo() {
        String username = sessionManager.getUsername();
        String email = sessionManager.getEmail();
        
        if (username != null && !username.isEmpty()) {
            etFullName.setText(username);
        }
        
        if (email != null && !email.isEmpty()) {
            etEmail.setText(email);
        }
    }

    private void loadCartItems() {
        progressBar.setVisibility(View.VISIBLE);
        
        String userId = sessionManager.getUserId();
        
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    cartItems.clear();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        CartItem item = document.toObject(CartItem.class);
                        cartItems.add(item);
                    }

                    progressBar.setVisibility(View.GONE);
                    
                    if (cartItems.isEmpty()) {
                        Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        updateOrderSummary();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("CheckoutActivity", "Error loading cart: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to load cart items", Toast.LENGTH_SHORT).show();
                });
    }

    private void updateOrderSummary() {
        double subtotal = 0;
        int itemCount = 0;
        
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
            itemCount += item.getQuantity();
        }
        
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;
        
        tvOrderSubtotal.setText(String.format("$%.2f", subtotal));
        tvOrderTax.setText(String.format("$%.2f", tax));
        tvOrderTotal.setText(String.format("$%.2f", total));
        tvItemCount.setText(itemCount + " item(s)");
    }

    private void validateAndPlaceOrder() {
        String fullName = etFullName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String city = etCity.getText().toString().trim();
        String zipCode = etZipCode.getText().toString().trim();

        if (fullName.isEmpty()) {
            etFullName.setError("Full name is required");
            etFullName.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (address.isEmpty()) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return;
        }

        if (city.isEmpty()) {
            etCity.setError("City is required");
            etCity.requestFocus();
            return;
        }

        if (zipCode.isEmpty()) {
            etZipCode.setError("Zip code is required");
            etZipCode.requestFocus();
            return;
        }

        int selectedPaymentId = rgPaymentMethod.getCheckedRadioButtonId();
        if (selectedPaymentId == -1) {
            Toast.makeText(this, "Please select a payment method", Toast.LENGTH_SHORT).show();
            return;
        }

        String paymentMethod = "Cash on Delivery";
        if (selectedPaymentId == R.id.rb_credit_card) {
            paymentMethod = "Credit Card";
        } else if (selectedPaymentId == R.id.rb_debit_card) {
            paymentMethod = "Debit Card";
        } else if (selectedPaymentId == R.id.rb_upi) {
            paymentMethod = "UPI";
        }

        placeOrder(fullName, email, phone, address, city, zipCode, paymentMethod);
    }

    private void placeOrder(String fullName, String email, String phone, 
                           String address, String city, String zipCode, String paymentMethod) {
        progressBar.setVisibility(View.VISIBLE);
        btnPlaceOrder.setEnabled(false);

        double subtotal = 0;
        int itemCount = 0;
        
        for (CartItem item : cartItems) {
            subtotal += item.getTotalPrice();
            itemCount += item.getQuantity();
        }
        
        double tax = subtotal * TAX_RATE;
        double total = subtotal + tax;

        String userId = sessionManager.getUserId();
        String username = sessionManager.getUsername();

        Order order = new Order();
        order.setUserId(Integer.parseInt(userId));
        order.setUsername(username);
        order.setTotalAmount(total);
        order.setStatus("Pending");
        order.setOrderDate(System.currentTimeMillis());

        List<Order.OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : cartItems) {
            Order.OrderItem orderItem = new Order.OrderItem(
                    cartItem.getWatchId(),
                    cartItem.getWatchName(),
                    cartItem.getQuantity(),
                    cartItem.getWatchPrice()
            );
            orderItems.add(orderItem);
        }
        order.setItems(orderItems);

        Map<String, Object> orderData = new HashMap<>();
        orderData.put("userId", userId);
        orderData.put("username", username);
        orderData.put("totalAmount", total);
        orderData.put("status", "Pending");
        orderData.put("orderDate", System.currentTimeMillis());
        orderData.put("fullName", fullName);
        orderData.put("email", email);
        orderData.put("phone", phone);
        orderData.put("address", address);
        orderData.put("city", city);
        orderData.put("zipCode", zipCode);
        orderData.put("paymentMethod", paymentMethod);
        
        List<Map<String, Object>> itemsData = new ArrayList<>();
        for (Order.OrderItem item : orderItems) {
            Map<String, Object> itemMap = new HashMap<>();
            itemMap.put("watchId", item.getWatchId());
            itemMap.put("watchName", item.getWatchName());
            itemMap.put("quantity", item.getQuantity());
            itemMap.put("price", item.getPrice());
            itemsData.add(itemMap);
        }
        orderData.put("items", itemsData);

        db.collection("orders")
                .add(orderData)
                .addOnSuccessListener(documentReference -> {
                    Log.d("CheckoutActivity", "Order placed successfully: " + documentReference.getId());
                    clearCart();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    btnPlaceOrder.setEnabled(true);
                    Log.e("CheckoutActivity", "Error placing order: " + e.getMessage(), e);
                    Toast.makeText(this, "Failed to place order. Please try again.", Toast.LENGTH_SHORT).show();
                });
    }

    private void clearCart() {
        String userId = sessionManager.getUserId();
        
        db.collection("cart")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    WriteBatch batch = db.batch();
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        batch.delete(document.getReference());
                    }
                    
                    batch.commit()
                            .addOnSuccessListener(aVoid -> {
                                progressBar.setVisibility(View.GONE);
                                showOrderConfirmation();
                            })
                            .addOnFailureListener(e -> {
                                progressBar.setVisibility(View.GONE);
                                Log.e("CheckoutActivity", "Error clearing cart: " + e.getMessage());
                                showOrderConfirmation();
                            });
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Log.e("CheckoutActivity", "Error finding cart items: " + e.getMessage());
                    showOrderConfirmation();
                });
    }

    private void showOrderConfirmation() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Order Placed Successfully!")
                .setMessage("Thank you for your order. We'll send you a confirmation email shortly.")
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(this, UserDashboardActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                })
                .setCancelable(false)
                .show();
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
}
