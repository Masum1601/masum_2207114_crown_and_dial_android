package com.example.watchstore_android_114;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.watchstore_android_114.utils.SessionManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        sessionManager = SessionManager.getInstance(this);

        createDefaultAdminIfNeeded();

        if (mAuth.getCurrentUser() != null) {
            navigateToMain();
            return;
        }

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);

        if (etEmail == null || etPassword == null || btnLogin == null || tvRegister == null) {
            Toast.makeText(this, "Error: Views not found", Toast.LENGTH_LONG).show();
            return;
        }

        btnLogin.setOnClickListener(v -> loginUser());
        tvRegister.setOnClickListener(v -> navigateToRegister());
    }

    private void loginUser() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                fetchUserDataAndNavigate(user.getUid());
                            }
                        } else {
                            btnLogin.setEnabled(true);
                            btnLogin.setText("Login");
                            Toast.makeText(LoginActivity.this,
                                    "Login failed: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void fetchUserDataAndNavigate(String userId) {
        Log.d("LoginActivity", "Fetching user data for ID: " + userId);
        
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                String username = "User";
                boolean isAdmin = false;
                
                Log.d("LoginActivity", "Document exists: " + documentSnapshot.exists());
                
                if (documentSnapshot.exists()) {
                    username = documentSnapshot.getString("username");
                    Boolean adminValue = documentSnapshot.getBoolean("isAdmin");
                    isAdmin = adminValue != null && adminValue;
                    
                    Log.d("LoginActivity", "Username: " + username + ", isAdmin: " + isAdmin);
                    
                    String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";
                    
                    if ("admin@watchstore.com".equals(email) && !isAdmin) {
                        Log.d("LoginActivity", "Fixing admin account - setting isAdmin to true");
                        Toast.makeText(this, "Fixing admin account...", Toast.LENGTH_SHORT).show();
                        
                        Map<String, Object> updateData = new HashMap<>();
                        updateData.put("isAdmin", true);
                        updateData.put("username", "Admin");
                        updateData.put("email", email);
                        
                        db.collection("users").document(userId)
                            .set(updateData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Admin account fixed! Please login again.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                recreate();
                            });
                        return;
                    }
                    
                    Toast.makeText(this, "User: " + username + ", Admin: " + isAdmin, Toast.LENGTH_LONG).show();
                } else {
                    Log.e("LoginActivity", "User document does not exist in Firestore!");
                    
                    String email = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";
                    if ("admin@watchstore.com".equals(email)) {
                        Log.d("LoginActivity", "Creating admin user document");
                        Toast.makeText(this, "Creating admin account...", Toast.LENGTH_SHORT).show();
                        
                        Map<String, Object> adminData = new HashMap<>();
                        adminData.put("username", "Admin");
                        adminData.put("email", email);
                        adminData.put("isAdmin", true);
                        
                        db.collection("users").document(userId)
                            .set(adminData)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(this, "Admin account created! Please login again.", Toast.LENGTH_LONG).show();
                                mAuth.signOut();
                                recreate();
                            });
                        return;
                    }
                    
                    Toast.makeText(this, "User document not found!", Toast.LENGTH_LONG).show();
                }
                
                sessionManager.saveUserData(username != null ? username : "User", isAdmin);
                
                if (isAdmin) {
                    Log.d("LoginActivity", "Navigating to Admin Dashboard");
                    Toast.makeText(LoginActivity.this, "Welcome Admin!", Toast.LENGTH_SHORT).show();
                    navigateToAdminDashboard();
                } else {
                    Log.d("LoginActivity", "Navigating to User Dashboard");
                    Toast.makeText(LoginActivity.this, "Welcome User!", Toast.LENGTH_SHORT).show();
                    navigateToMain();
                }
            })
            .addOnFailureListener(e -> {
                sessionManager.saveUserData("User", false);
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            });
    }

    private void navigateToAdminDashboard() {
        Intent intent = new Intent(LoginActivity.this, AdminDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, UserDashboardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToRegister() {
        Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(intent);
    }

    private void createDefaultAdminIfNeeded() {
        db.collection("users")
            .whereEqualTo("isAdmin", true)
            .limit(1)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (queryDocumentSnapshots.isEmpty()) {
                    createDefaultAdmin();
                }
            });
    }

    private void createDefaultAdmin() {
        String adminEmail = "admin@watchstore.com";
        String adminPassword = "admin123";

        mAuth.createUserWithEmailAndPassword(adminEmail, adminPassword)
            .addOnSuccessListener(authResult -> {
                if (authResult.getUser() != null) {
                    Map<String, Object> adminData = new HashMap<>();
                    adminData.put("username", "Admin");
                    adminData.put("email", adminEmail);
                    adminData.put("isAdmin", true);

                    db.collection("users").document(authResult.getUser().getUid())
                        .set(adminData)
                        .addOnSuccessListener(aVoid -> {
                            mAuth.signOut();
                        });
                }
            })
            .addOnFailureListener(e -> {
            });
    }
}
