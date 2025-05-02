package com.secure.messenger.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.secure.messenger.android.R;
import com.secure.messenger.android.ui.common.BaseActivity;
import com.secure.messenger.android.MainActivity;

/**
 * Екран реєстрації нового користувача
 */
public class RegisterActivity extends BaseActivity {

    public static final String EXTRA_VERIFIED_PHONE_NUMBER = "ЦЕЙ_КЛЮЧ_ВЕРИФІКОВАНО";
    private RegisterViewModel viewModel;
    private Toolbar toolbar;
    private TextInputLayout layoutUsername;
    private TextInputEditText editUsername;
    private TextInputLayout layoutPhone;
    private TextInputEditText editPhone;
    private TextInputLayout layoutPassword;
    private TextInputEditText editPassword;
    private TextInputLayout layoutConfirmPassword;
    private TextInputEditText editConfirmPassword;
    private Button buttonRegister;
    private Button buttonLogin;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Ініціалізація ViewModel
        viewModel = new ViewModelProvider(this).get(RegisterViewModel.class);

        // Ініціалізація UI компонентів
        initViews();

        // Налаштування слухачів
        setupListeners();

        // Спостереження за даними ViewModel
        observeViewModel();
    }

    /**
     * Для екрану реєстрації автентифікація не потрібна
     */
    @Override
    protected boolean requiresAuth() {
        return false;
    }

    /**
     * Ініціалізація UI компонентів
     */
    private void initViews() {
        // Налаштування toolbar
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setupToolbar(toolbar, getString(R.string.title_register), true);
        }

        layoutUsername = findViewById(R.id.layout_username);
        editUsername = findViewById(R.id.edit_username);
        layoutPhone = findViewById(R.id.layout_phone);
        editPhone = findViewById(R.id.edit_phone);
        layoutPassword = findViewById(R.id.layout_password);
        editPassword = findViewById(R.id.edit_password);
        layoutConfirmPassword = findViewById(R.id.layout_confirm_password);
        editConfirmPassword = findViewById(R.id.edit_confirm_password);
        buttonRegister = findViewById(R.id.button_register);
        buttonLogin = findViewById(R.id.button_login);
        progressBar = findViewById(R.id.progress_bar);
    }

    /**
     * Налаштування слухачів подій
     */
    private void setupListeners() {
        buttonRegister.setOnClickListener(v -> attemptRegister());
        buttonLogin.setOnClickListener(v -> navigateToLogin());

        // Обробка введення для очищення помилок
        editUsername.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) layoutUsername.setError(null);
        });

        editPhone.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) layoutPhone.setError(null);
        });

        editPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) layoutPassword.setError(null);
        });

        editConfirmPassword.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) layoutConfirmPassword.setError(null);
        });
    }

    /**
     * Спостереження за змінами даних у ViewModel
     */
    private void observeViewModel() {
        // Спостереження за статусом завантаження
        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            buttonRegister.setEnabled(!isLoading);
            buttonLogin.setEnabled(!isLoading);

            // Блокування полів під час завантаження
            setInputFieldsEnabled(!isLoading);
        });

        // Спостереження за результатом реєстрації
        viewModel.getRegistrationResult().observe(this, result -> {
            if (result.isSuccess()) {
                showToast(getString(R.string.registration_success));
                navigateToMain();
            } else {
                showRegistrationError(result.getErrorMessage());
            }
        });

        // Спостереження за помилками
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                showRegistrationError(error);
                viewModel.errorHandled();
            }
        });
    }

    /**
     * Блокує або розблоковує поля введення
     *
     * @param enabled статус активності полів
     */
    private void setInputFieldsEnabled(boolean enabled) {
        editUsername.setEnabled(enabled);
        editPhone.setEnabled(enabled);
        editPassword.setEnabled(enabled);
        editConfirmPassword.setEnabled(enabled);
    }

    /**
     * Відображає помилку реєстрації
     *
     * @param errorMessage текст помилки
     */
    private void showRegistrationError(String errorMessage) {
        // Показуємо помилку згідно її типу
        if (errorMessage.contains("ім'я користувача") ||
                errorMessage.contains("користувач вже існує") ||
                errorMessage.contains("username")) {
            layoutUsername.setError(errorMessage);
        } else if (errorMessage.contains("номер телефону") ||
                errorMessage.contains("телефон вже зареєстрований") ||
                errorMessage.contains("phone")) {
            layoutPhone.setError(errorMessage);
        } else if (errorMessage.contains("пароль") ||
                errorMessage.contains("password")) {
            layoutPassword.setError(errorMessage);
        } else {
            // Загальні помилки показуємо як Toast
            showToast(errorMessage);
        }
    }

    /**
     * Спроба реєстрації користувача
     */
    private void attemptRegister() {
        // Очищення помилок з полів
        layoutUsername.setError(null);
        layoutPhone.setError(null);
        layoutPassword.setError(null);
        layoutConfirmPassword.setError(null);

        // Отримання введених даних
        String username = editUsername.getText().toString().trim();
        String phone = editPhone.getText().toString().trim();
        String password = editPassword.getText().toString();
        String confirmPassword = editConfirmPassword.getText().toString();

        // Локальна валідація полів
        boolean valid = true;

        if (TextUtils.isEmpty(username)) {
            layoutUsername.setError(getString(R.string.error_empty_username));
            valid = false;
        }

        if (TextUtils.isEmpty(phone)) {
            layoutPhone.setError(getString(R.string.error_empty_phone));
            valid = false;
        }

        if (TextUtils.isEmpty(password)) {
            layoutPassword.setError(getString(R.string.error_empty_password));
            valid = false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            layoutConfirmPassword.setError(getString(R.string.error_empty_confirm_password));
            valid = false;
        } else if (!password.equals(confirmPassword)) {
            layoutConfirmPassword.setError(getString(R.string.error_passwords_not_match));
            valid = false;
        }

        if (valid) {
            // Приховуємо клавіатуру перед запитом
            hideKeyboard();

            // Виклик методу реєстрації у ViewModel
            viewModel.register(username, phone, password, confirmPassword);
        }
    }

    /**
     * Приховує клавіатуру
     */
    private void hideKeyboard() {
        View currentFocus = getCurrentFocus();
        if (currentFocus != null) {
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager)
                    getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(currentFocus.getWindowToken(), 0);
        }
    }

    /**
     * Перехід до екрану входу
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Перехід до головного екрану
     */
    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}