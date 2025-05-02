package com.secure.messenger.android.ui.common;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.secure.messenger.android.data.local.PreferenceManager;
import com.secure.messenger.android.ui.auth.LoginActivity;

/**
 * Базовий клас для всіх активностей додатку
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected PreferenceManager preferenceManager;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Ініціалізація PreferenceManager
        preferenceManager = new PreferenceManager(this);

        // Перевірка автентифікації
        if (requiresAuth() && !isAuthenticated()) {
            redirectToLogin();
            return;
        }
    }

    /**
     * Чи потребує активність автентифікації
     *
     * @return true, якщо активність потребує автентифікації
     */
    protected boolean requiresAuth() {
        return true;
    }

    /**
     * Перевірка, чи автентифікований користувач
     *
     * @return true, якщо користувач автентифікований
     */
    protected boolean isAuthenticated() {
        return preferenceManager.isUserLoggedIn();
    }

    /**
     * Перенаправлення на екран входу
     */
    protected void redirectToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Налаштування панелі інструментів (Toolbar)
     *
     * @param toolbar панель інструментів
     * @param title заголовок
     * @param showBackButton показувати кнопку "Назад"
     */
    protected void setupToolbar(Toolbar toolbar, String title, boolean showBackButton) {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(showBackButton);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Показати діалог прогресу
     *
     * @param message повідомлення
     */
    protected void showProgress(String message) {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setCancelable(false);
        }

        progressDialog.setMessage(message);

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    /**
     * Сховати діалог прогресу
     */
    protected void hideProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    /**
     * Показати коротке повідомлення
     *
     * @param message текст повідомлення
     */
    protected void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Показати довге повідомлення
     *
     * @param message текст повідомлення
     */
    protected void showLongToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    /**
     * Вийти з системи
     */
    protected void logout() {
        preferenceManager.clearAuthData();
        redirectToLogin();
    }

    @Override
    protected void onDestroy() {
        hideProgress();
        super.onDestroy();
    }
}