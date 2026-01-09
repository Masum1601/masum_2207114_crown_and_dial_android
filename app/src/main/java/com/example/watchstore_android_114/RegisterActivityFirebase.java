package com.example.watchstore_android_114;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.watchstore_android_114.utils.FirebaseHelper;
import com.example.watchstore_android_114.utils.SessionManager;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivityFirebase extends AppCompatActivity {

    private EditText usernameInput, emailInput, passwordInput, confirmPasswordInput;
    private Button registerButton;
    private TextView loginLink;
    private FirebaseHelper firebaseHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseHelper = FirebaseHelper.getInstance();
        sessionManager = SessionManager.getInstance(this);

        usernameInput = findViewById(R.id.et_username);
        emailInput = findViewById(R.id.et_email);
        passwordInput = findViewById(R.id.et_password);
        confirmPasswordInput = findViewById(R.id.et_confirm_password);
        registerButton = findViewById(R.id.btn_register);
        loginLink = findViewById(R.id.tv_back_to_login);

        registerButton.setOnClickListener(v -> registerUser());
        loginLink.setOnClickListener(v -> navigateToLogin());
    }

    private void registerUser() {
        String username = usernameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            usernameInput.setError("Username is required");
            usernameInput.requestFocus();
            return;
        }

        if (username.length() < 3) {
            usernameInput.setError("Username must be at least 3 characters");
            usernameInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            emailInput.requestFocus();
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            emailInput.setError("Please enter a valid email");
            emailInput.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            passwordInput.setError("Password is required");
            passwordInput.requestFocus();
            return;
        }

        if (password.length() < 6) {
            passwordInput.setError("Password must be at least 6 characters");
            passwordInput.requestFocus();
            return;
        }

        if (!password.equals(confirmPassword)) {
            confirmPasswordInput.setError("Passwords do not match");
            confirmPasswordInput.requestFocus();
            return;
        }

        registerButton.setEnabled(false);
        registerButton.setText("Registering...");

        firebaseHelper.registerUser(email, password, username, new FirebaseHelper.AuthCallback() {
            @Override
            public void onSuccess(FirebaseUser user) {
                sessionManager.saveUserData(username, false);

                Toast.makeText(RegisterActivityFirebase.this, 
                    "Registration successful!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onFailure(String errorMessage) {
                registerButton.setEnabled(true);
                registerButton.setText("Register");
                
                String userFriendlyMessage = parseErrorMessage(errorMessage);
                Toast.makeText(RegisterActivityFirebase.this, 
                    "Registration failed: " + userFriendlyMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    private String parseErrorMessage(String errorMessage) {
        if (errorMessage == null) return "Unknown error occurred";
        
        if (errorMessage.contains("email address is already in use")) {
            return "This email is already registered. Please login instead.";
        } else if (errorMessage.contains("email address is badly formatted")) {
            return "Invalid email format";
        } else if (errorMessage.contains("weak password")) {
            return "Password is too weak. Use at least 6 characters.";
        } else if (errorMessage.contains("network error")) {
            return "Network error. Please check your internet connection.";
        }
        
        return errorMessage;
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegisterActivityFirebase.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(RegisterActivityFirebase.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
