package com.secure.messenger.android.data.local;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Клас для безпечного зберігання та управління токенами автентифікації
 */
public class TokenManager {
    private static final String TAG = "TokenManager";

    private static final String PREF_FILE_NAME = "secure_messenger_tokens";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_EXPIRES_AT = "expires_at";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";

    private final SharedPreferences encryptedPrefs;

    /**
     * Конструктор, який створює захищені SharedPreferences
     *
     * @param context контекст додатка
     */
    public TokenManager(Context context) {
        SharedPreferences prefs;
        try {
            // Створення ключа шифрування для EncryptedSharedPreferences
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .setKeyGenParameterSpec(new KeyGenParameterSpec.Builder(
                            "_encrypted_token_store_key_",
                            KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                            .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                            .setKeySize(256)
                            .build())
                    .build();

            // Створення EncryptedSharedPreferences
            prefs = EncryptedSharedPreferences.create(
                    context,
                    PREF_FILE_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );

            Log.d(TAG, "EncryptedSharedPreferences initialized successfully");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences: " + e.getMessage(), e);
            // Якщо не вдалося створити захищене сховище, повертаємося до звичайного
            prefs = context.getSharedPreferences(PREF_FILE_NAME, Context.MODE_PRIVATE);
        }

        this.encryptedPrefs = prefs;
    }

    /**
     * Конструктор для тестування
     */
    public TokenManager(SharedPreferences prefs) {
        this.encryptedPrefs = prefs;
    }

    /**
     * Зберігає токени автентифікації
     *
     * @param accessToken токен доступу
     * @param refreshToken токен оновлення
     * @param expiresAt час закінчення дії токена (мілісекунди)
     */
    public void saveTokens(String accessToken, String refreshToken, long expiresAt) {
        encryptedPrefs.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putLong(KEY_EXPIRES_AT, expiresAt)
                .apply();

        Log.d(TAG, "Tokens saved successfully");
    }

    /**
     * Зберігає дані про користувача
     *
     * @param userId ідентифікатор користувача
     * @param username ім'я користувача
     */
    public void saveUserInfo(String userId, String username) {
        encryptedPrefs.edit()
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USERNAME, username)
                .apply();

        Log.d(TAG, "User info saved successfully");
    }

    /**
     * Отримує токен доступу
     *
     * @return токен доступу або null, якщо не існує
     */
    public String getAccessToken() {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Отримує токен оновлення
     *
     * @return токен оновлення або null, якщо не існує
     */
    public String getRefreshToken() {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Отримує ідентифікатор користувача
     *
     * @return ідентифікатор користувача або null, якщо не існує
     */
    public String getUserId() {
        return encryptedPrefs.getString(KEY_USER_ID, null);
    }

    /**
     * Отримує ім'я користувача
     *
     * @return ім'я користувача або null, якщо не існує
     */
    public String getUsername() {
        return encryptedPrefs.getString(KEY_USERNAME, null);
    }

    /**
     * Перевіряє, чи користувач автентифікований
     *
     * @return true, якщо користувач автентифікований
     */
    public boolean isLoggedIn() {
        return getAccessToken() != null;
    }

    /**
     * Перевіряє, чи токен протермінований
     *
     * @return true, якщо токен протермінований
     */
    public boolean isTokenExpired() {
        long expiresAt = encryptedPrefs.getLong(KEY_EXPIRES_AT, 0);
        if (expiresAt == 0) {
            return true;
        }

        LocalDateTime expirationTime = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(expiresAt), ZoneId.systemDefault());
        return LocalDateTime.now().isAfter(expirationTime);
    }

    /**
     * Очищає всі токени та дані користувача
     */
    public void clearTokens() {
        encryptedPrefs.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_EXPIRES_AT)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .apply();

        Log.d(TAG, "Tokens cleared successfully");
    }
}