package com.secure.messenger.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Менеджер для безпечного зберігання налаштувань додатку
 */
public class PreferenceManager {
    private static final String TAG = "PreferenceManager";

    private static final String PREFERENCES_FILE = "secure_messenger_prefs";
    private static final String ENCRYPTED_PREFERENCES_FILE = "secure_messenger_encrypted_prefs";

    // Ключі для звичайних налаштувань
    private static final String KEY_NOTIFICATION_ENABLED = "notification_enabled";
    private static final String KEY_THEME_MODE = "theme_mode";
    private static final String KEY_LANGUAGE = "language";

    // Ключі для шифрованих налаштувань
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_KEYS_GENERATED = "keys_generated";

    private final SharedPreferences preferences;
    private final SharedPreferences encryptedPreferences;

    public PreferenceManager(Context context) {
        preferences = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);

        // Ініціалізація шифрованих налаштувань
        SharedPreferences encryptedPrefs = null;
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setKeyGenParameterSpec(new KeyGenParameterSpec.Builder(
                            MasterKey.DEFAULT_MASTER_KEY_ALIAS,
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .build())
                    .build();

            encryptedPrefs = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFERENCES_FILE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing encrypted preferences: " + e.getMessage(), e);
        }

        encryptedPreferences = encryptedPrefs != null ? encryptedPrefs :
                context.getSharedPreferences(ENCRYPTED_PREFERENCES_FILE, Context.MODE_PRIVATE);
    }

    /**
     * Зберігає токен авторизації
     * @param token токен для зберігання
     */
    public void saveAuthToken(@NonNull String token) {
        encryptedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    /**
     * Отримує збережений токен авторизації
     * @return токен або null, якщо не знайдено
     */
    public String getAuthToken() {
        return encryptedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Зберігає токен оновлення
     * @param token токен для зберігання
     */
    public void saveRefreshToken(@NonNull String token) {
        encryptedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }

    /**
     * Отримує збережений токен оновлення
     * @return токен або null, якщо не знайдено
     */
    public String getRefreshToken() {
        return encryptedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Очищає всі дані автентифікації
     */
    public void clearAuthData() {
        encryptedPreferences.edit()
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .apply();
    }

    /**
     * Зберігає ID користувача
     * @param userId ID користувача
     */
    public void saveUserId(@NonNull String userId) {
        encryptedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    /**
     * Отримує збережений ID користувача
     * @return ID користувача або null, якщо не знайдено
     */
    public String getUserId() {
        return encryptedPreferences.getString(KEY_USER_ID, null);
    }

    /**
     * Зберігає ім'я користувача
     * @param username ім'я користувача
     */
    public void saveUsername(@NonNull String username) {
        encryptedPreferences.edit().putString(KEY_USERNAME, username).apply();
    }

    /**
     * Отримує збережене ім'я користувача
     * @return ім'я користувача або null, якщо не знайдено
     */
    public String getUsername() {
        return encryptedPreferences.getString(KEY_USERNAME, null);
    }

    /**
     * Зберігає номер телефону користувача
     * @param phoneNumber номер телефону
     */
    public void savePhoneNumber(@NonNull String phoneNumber) {
        encryptedPreferences.edit().putString(KEY_PHONE_NUMBER, phoneNumber).apply();
    }

    /**
     * Отримує збережений номер телефону користувача
     * @return номер телефону або null, якщо не знайдено
     */
    public String getPhoneNumber() {
        return encryptedPreferences.getString(KEY_PHONE_NUMBER, null);
    }

    /**
     * Встановлює прапорець генерації ключів
     * @param generated true, якщо ключі згенеровані
     */
    public void setKeysGenerated(boolean generated) {
        encryptedPreferences.edit().putBoolean(KEY_KEYS_GENERATED, generated).apply();
    }

    /**
     * Перевіряє, чи згенеровані ключі
     * @return true, якщо ключі згенеровані
     */
    public boolean isKeysGenerated() {
        return encryptedPreferences.getBoolean(KEY_KEYS_GENERATED, false);
    }

    /**
     * Включає або виключає повідомлення
     * @param enabled true, якщо повідомлення включені
     */
    public void setNotificationsEnabled(boolean enabled) {
        preferences.edit().putBoolean(KEY_NOTIFICATION_ENABLED, enabled).apply();
    }

    /**
     * Перевіряє, чи включені повідомлення
     * @return true, якщо повідомлення включені
     */
    public boolean areNotificationsEnabled() {
        return preferences.getBoolean(KEY_NOTIFICATION_ENABLED, true);
    }

    /**
     * Встановлює тему додатку
     * @param themeMode режим теми (0 - світла, 1 - темна, 2 - системна)
     */
    public void setThemeMode(int themeMode) {
        preferences.edit().putInt(KEY_THEME_MODE, themeMode).apply();
    }

    /**
     * Отримує встановлену тему додатку
     * @return режим теми (0 - світла, 1 - темна, 2 - системна)
     */
    public int getThemeMode() {
        return preferences.getInt(KEY_THEME_MODE, 2); // За замовчуванням - системна
    }

    /**
     * Встановлює мову додатку
     * @param languageCode код мови
     */
    public void setLanguage(@NonNull String languageCode) {
        preferences.edit().putString(KEY_LANGUAGE, languageCode).apply();
    }

    /**
     * Отримує встановлену мову додатку
     * @return код мови або null, якщо використовується системна мова
     */
    public String getLanguage() {
        return preferences.getString(KEY_LANGUAGE, null);
    }

    /**
     * Перевіряє, чи користувач авторизований
     * @return true, якщо користувач авторизований
     */
    public boolean isUserLoggedIn() {
        return getAuthToken() != null && getUserId() != null;
    }
}