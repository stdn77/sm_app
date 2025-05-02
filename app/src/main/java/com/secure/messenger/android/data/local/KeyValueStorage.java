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
 * Клас для безпечного зберігання та отримання критичних даних додатка
 */
public class KeyValueStorage {
    private static final String TAG = "KeyValueStorage";

    private static final String ENCRYPTED_PREFS_FILENAME = "secure_messenger_encrypted_prefs";
    private static final String DEVICE_PREFS_FILENAME = "secure_messenger_device_prefs";

    // Ключі для секретних даних
    private static final String KEY_AUTH_TOKEN = "auth_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_EXPIRATION = "token_expiration";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_USER_PHONE = "user_phone";
    private static final String KEY_USER_PUBLIC_KEY = "user_public_key";
    private static final String KEY_USER_STATUS = "user_status";

    // Ключі для несекретних даних
    private static final String KEY_DEVICE_ID = "device_id";

    private final SharedPreferences encryptedPrefs;
    private final SharedPreferences devicePrefs;

    /**
     * Конструктор
     *
     * @param context контекст додатка
     */
    public KeyValueStorage(Context context) {
        // Ініціалізація звичайних SharedPreferences
        devicePrefs = context.getSharedPreferences(DEVICE_PREFS_FILENAME, Context.MODE_PRIVATE);

        // Ініціалізація шифрованих SharedPreferences
        SharedPreferences encrypted = null;
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

            encrypted = EncryptedSharedPreferences.create(
                    context,
                    ENCRYPTED_PREFS_FILENAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            Log.d(TAG, "EncryptedSharedPreferences successfully initialized");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Error initializing EncryptedSharedPreferences: " + e.getMessage(), e);
            // Якщо не вдалося створити зашифровані преференції, використовуємо звичайні
            encrypted = context.getSharedPreferences(ENCRYPTED_PREFS_FILENAME, Context.MODE_PRIVATE);
        }
        encryptedPrefs = encrypted;
    }

    /**
     * Зберігає ідентифікатор пристрою
     *
     * @param deviceId ідентифікатор пристрою
     */
    public void saveDeviceId(String deviceId) {
        devicePrefs.edit().putString(KEY_DEVICE_ID, deviceId).apply();
    }

    /**
     * Отримує ідентифікатор пристрою
     *
     * @return ідентифікатор пристрою або null
     */
    public String getDeviceId() {
        return devicePrefs.getString(KEY_DEVICE_ID, null);
    }

    /**
     * Зберігає токен авторизації
     *
     * @param token токен авторизації
     */
    public void saveAuthToken(String token) {
        encryptedPrefs.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    /**
     * Отримує токен авторизації
     *
     * @return токен авторизації або null
     */
    public String getAuthToken() {
        return encryptedPrefs.getString(KEY_AUTH_TOKEN, null);
    }

    /**
     * Зберігає токен оновлення
     *
     * @param refreshToken токен оновлення
     */
    public void saveRefreshToken(String refreshToken) {
        encryptedPrefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    /**
     * Отримує токен оновлення
     *
     * @return токен оновлення або null
     */
    public String getRefreshToken() {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Зберігає час закінчення дії токена
     *
     * @param expirationTime час закінчення дії токена (в мілісекундах від епохи)
     */
    public void saveTokenExpirationTime(long expirationTime) {
        encryptedPrefs.edit().putLong(KEY_TOKEN_EXPIRATION, expirationTime).apply();
    }

    /**
     * Отримує час закінчення дії токена
     *
     * @return час закінчення дії токена або 0
     */
    public long getTokenExpirationTime() {
        return encryptedPrefs.getLong(KEY_TOKEN_EXPIRATION, 0);
    }

    /**
     * Зберігає ідентифікатор користувача
     *
     * @param userId ідентифікатор користувача
     */
    public void saveUserId(String userId) {
        encryptedPrefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    /**
     * Отримує ідентифікатор користувача
     *
     * @return ідентифікатор користувача або null
     */
    public String getUserId() {
        return encryptedPrefs.getString(KEY_USER_ID, null);
    }

    /**
     * Зберігає ім'я користувача
     *
     * @param username ім'я користувача
     */
    public void saveUsername(String username) {
        encryptedPrefs.edit().putString(KEY_USERNAME, username).apply();
    }

    /**
     * Отримує ім'я користувача
     *
     * @return ім'я користувача або null
     */
    public String getUsername() {
        return encryptedPrefs.getString(KEY_USERNAME, null);
    }

    /**
     * Зберігає номер телефону користувача
     *
     * @param phoneNumber номер телефону користувача
     */
    public void saveUserPhoneNumber(String phoneNumber) {
        encryptedPrefs.edit().putString(KEY_USER_PHONE, phoneNumber).apply();
    }

    /**
     * Отримує номер телефону користувача
     *
     * @return номер телефону користувача або null
     */
    public String getUserPhoneNumber() {
        return encryptedPrefs.getString(KEY_USER_PHONE, null);
    }

    /**
     * Зберігає публічний ключ користувача
     *
     * @param publicKey публічний ключ користувача
     */
    public void saveUserPublicKey(byte[] publicKey) {
        if (publicKey != null) {
            encryptedPrefs.edit().putString(KEY_USER_PUBLIC_KEY, android.util.Base64.encodeToString(publicKey, android.util.Base64.DEFAULT)).apply();
        } else {
            encryptedPrefs.edit().remove(KEY_USER_PUBLIC_KEY).apply();
        }
    }

    /**
     * Отримує публічний ключ користувача
     *
     * @return публічний ключ користувача або null
     */
    public byte[] getUserPublicKey() {
        String encodedKey = encryptedPrefs.getString(KEY_USER_PUBLIC_KEY, null);
        if (encodedKey != null) {
            return android.util.Base64.decode(encodedKey, android.util.Base64.DEFAULT);
        }
        return null;
    }

    /**
     * Зберігає статус користувача
     *
     * @param status статус користувача
     */
    public void saveUserStatus(String status) {
        encryptedPrefs.edit().putString(KEY_USER_STATUS, status).apply();
    }

    /**
     * Отримує статус користувача
     *
     * @return статус користувача або null
     */
    public String getUserStatus() {
        return encryptedPrefs.getString(KEY_USER_STATUS, null);
    }

    /**
     * Очищає всі авторизаційні дані
     */
    public void clearAuthData() {
        encryptedPrefs.edit()
                .remove(KEY_AUTH_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_TOKEN_EXPIRATION)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_USER_PHONE)
                .remove(KEY_USER_PUBLIC_KEY)
                .remove(KEY_USER_STATUS)
                .apply();
    }

    /**
     * Очищає всі дані
     */
    public void clearAll() {
        clearAuthData();
        devicePrefs.edit().clear().apply();
    }
}