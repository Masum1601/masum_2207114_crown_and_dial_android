package com.example.watchstore_android_114;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.watchstore_android_114.models.User;
import com.example.watchstore_android_114.utils.JSONDatabaseManager;
import com.example.watchstore_android_114.utils.SessionManager;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private Button btnLogin;
    private TextView tvRegister;
    private JSONDatabaseManager dbManager;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbManager = JSONDatabaseManager.getInstance(this);
        sessionManager = SessionManager.getInstance(this);

        if (sessionManager.isLoggedIn()) {
            navigateToDashboard();
            return;
        }

        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        tvRegister = findViewById(R.id.tv_register);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        tvRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty()) {
            etUsername.setError("Username is required");
            etUsername.requestFocus();
            return;
        }

        if (password.isEmpty()) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return;
        }

        User user = dbManager.getUserByUsername(username);

        if (user == null) {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!user.getPassword().equals(password)) {
            Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            return;
        }

        sessionManager.createSession(user.getId(), user.getUsername(), 
                                    user.getEmail(), user.isAdmin());

        Toast.makeText(this, "Welcome " + user.getUsername() + "!", Toast.LENGTH_SHORT).show();
        navigateToDashboard();
    }

    private void navigateToDashboard() {
        Toast.makeText(this, "Dashboard will be created next", Toast.LENGTH_SHORT).show();
        finish();
    }
}
