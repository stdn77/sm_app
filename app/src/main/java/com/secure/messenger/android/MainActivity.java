package com.secure.messenger.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;

import com.google.android.material.navigation.NavigationView;
import com.secure.messenger.android.data.local.PreferenceManager;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.ui.auth.LoginActivity;
import com.secure.messenger.android.ui.chat.ChatListFragment;
import com.secure.messenger.android.ui.group.GroupListFragment;
import com.secure.messenger.android.ui.settings.ProfileFragment;
import com.secure.messenger.android.ui.settings.SettingsFragment;

/**
 * Головна активність додатка
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "MainActivity";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;
    private TokenManager tokenManager;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tokenManager = new TokenManager(this);
        preferenceManager = new PreferenceManager(this);

        // Перевіряємо, чи користувач авторизований
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        initViews();
        setupNavigation();

        // За замовчуванням відкриваємо список чатів
        if (savedInstanceState == null) {
            navigateTo(new ChatListFragment());
            navigationView.setCheckedItem(R.id.nav_chats);
        }
    }

    /**
     * Ініціалізація UI-компонентів
     */
    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        // Встановлюємо дані користувача в шапці меню
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.nav_header_username);
        TextView navPhone = headerView.findViewById(R.id.nav_header_phone);

        String username = tokenManager.getUsername();
        if (username != null) {
            navUsername.setText(username);
        }

        // Додаємо відображення номера телефону
        if (preferenceManager.getPhoneNumber() != null) {
            navPhone.setText(preferenceManager.getPhoneNumber());
        }
    }

    /**
     * Налаштування бокової навігації
     */
    private void setupNavigation() {
        // Налаштування кнопки-гамбургера
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Встановлюємо обробник вибору пунктів меню
        navigationView.setNavigationItemSelectedListener(this);
    }

    /**
     * Обробка вибору пункту меню в боковій навігації
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_chats) {
            navigateTo(new ChatListFragment());
        } else if (id == R.id.nav_groups) {
            navigateTo(new GroupListFragment());
        } else if (id == R.id.nav_profile) {
            navigateTo(new ProfileFragment());
        } else if (id == R.id.nav_settings) {
            navigateTo(new SettingsFragment());
        } else if (id == R.id.nav_logout) {
            logout();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    /**
     * Перехід до іншого фрагмента
     */
    private void navigateTo(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }

    /**
     * Вихід з облікового запису
     */
    private void logout() {
        Log.d(TAG, "Logging out...");
        tokenManager.clearTokens();
        navigateToLogin();
    }

    /**
     * Перехід до екрану входу
     */
    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Обробка натискання кнопки "Назад"
     */
    @Override
    public void onBackPressed() {
        // Якщо відкрите бокове меню, закриваємо його
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}