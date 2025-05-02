package com.secure.messenger.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.secure.messenger.android.R;
import com.secure.messenger.android.MainActivity;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private TextInputEditText editUsername;
    private TextInputEditText editPassword;
    private Button buttonLogin;
    private Button buttonRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Ініціалізація ViewModel
        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);

        // Ініціалізація UI компонентів
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonRegister = findViewById(R.id.buttonRegister);

        // Налаштування слухачів
        buttonLogin.setOnClickListener(v -> attemptLogin());
        buttonRegister.setOnClickListener(v -> startRegistration());

        // Спостереження за станом авторизації
        viewModel.getAuthResultLiveData().observe(this, authResult -> {
            if (authResult.isSuccess()) {
                navigateToChatList();
            } else {
                showError(authResult.getErrorMessage());
            }
        });
    }

    private void attemptLogin() {
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        // Валідація введених даних
        if (TextUtils.isEmpty(username)) {
            editUsername.setError("Вкажіть ім'я користувача");
            return;
        }

        if (TextUtils.isEmpty(password)) {
            editPassword.setError("Вкажіть пароль");
            return;
        }

        // Виконання входу
        viewModel.login(username, password);
    }

    private void startRegistration() {
        Intent intent = new Intent(this, PhoneVerificationActivity.class);
        startActivity(intent);
    }

    private void navigateToChatList() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}