package com.secure.messenger.android.data.repository;

import android.content.Context;
import android.util.Log;

import com.secure.messenger.android.data.api.AuthServiceClient;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.util.SecurityUtils;
import com.secure.messenger.proto.AuthResponse;
import com.secure.messenger.proto.LoginRequest;
import com.secure.messenger.proto.RegisterRequest;
import com.secure.messenger.proto.RefreshTokenRequest;

import com.google.protobuf.ByteString;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторій для автентифікації користувачів
 */
public class AuthRepository {
    private static final String TAG = "AuthRepository";

    private final AuthServiceClient authServiceClient;
    private final TokenManager tokenManager;
    private final Executor executor;
    private final Context context;

    /**
     * Створює новий екземпляр репозиторію автентифікації
     *
     * @param context контекст додатка
     * @param authServiceClient клієнт для сервісу автентифікації
     */
    public AuthRepository(Context context, AuthServiceClient authServiceClient) {
        this.context = context;
        this.authServiceClient = authServiceClient;
        this.tokenManager = new TokenManager(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Перевіряє, чи автентифікований користувач
     *
     * @return true, якщо користувач автентифікований
     */
    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn() && !tokenManager.isTokenExpired();
    }

    /**
     * Виконує вхід користувача
     *
     * @param username ім'я користувача
     * @param password пароль
     * @param callback колбек з результатом операції
     */
    public void login(String username, String password, AuthCallback callback) {
        executor.execute(() -> {
            try {
                // Створення запиту на логін
                String deviceId = getOrGenerateDeviceId();

                LoginRequest request = LoginRequest.newBuilder()
                        .setUsername(username)
                        .setPassword(password)
                        .setDeviceId(ByteString.copyFromUtf8(deviceId))
                        .setDeviceName(android.os.Build.MODEL)
                        .build();

                // Виконання запиту
                AuthResponse response = authServiceClient.login(request);

                // Збереження токенів
                saveAuthResponse(response);

                // Оповіщення про успішний вхід
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Login error: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Виконує реєстрацію нового користувача
     *
     * @param username ім'я користувача
     * @param phoneNumber номер телефону
     * @param password пароль
     * @param callback колбек з результатом операції
     */
    public void register(String username, String phoneNumber, String password, AuthCallback callback) {
        executor.execute(() -> {
            try {
                // Генерація ключової пари для E2EE
                byte[] publicKey = SecurityUtils.getPublicKeyForServer();
                if (publicKey == null) {
                    throw new RuntimeException("Failed to generate encryption keys");
                }

                // Генерація ідентифікатора пристрою, якщо він ще не існує
                String deviceId = getOrGenerateDeviceId();

                // Створення запиту на реєстрацію
                RegisterRequest request = RegisterRequest.newBuilder()
                        .setUsername(username)
                        .setPhoneNumber(phoneNumber)
                        .setPassword(password)
                        .setPublicKey(ByteString.copyFrom(publicKey))
                        .setDeviceId(ByteString.copyFromUtf8(deviceId))
                        .setDeviceName(android.os.Build.MODEL)
                        .build();

                // Виконання запиту
                AuthResponse response = authServiceClient.register(request);

                // Збереження токенів та інформації про користувача
                saveAuthResponse(response);

                // Оповіщення про успішну реєстрацію
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Registration error: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Оновлює токени, якщо вони закінчились
     *
     * @param callback колбек з результатом операції
     */
    public void refreshTokenIfNeeded(AuthCallback callback) {
        // Перевіряємо, чи токен протермінований
        if (tokenManager.isTokenExpired()) {
            refreshToken(callback);
        } else {
            callback.onSuccess();
        }
    }

    /**
     * Оновлює токени
     *
     * @param callback колбек з результатом операції
     */
    public void refreshToken(AuthCallback callback) {
        executor.execute(() -> {
            try {
                String refreshToken = tokenManager.getRefreshToken();
                if (refreshToken == null) {
                    callback.onError("No refresh token available");
                    return;
                }

                // Створення запиту на оновлення токена
                RefreshTokenRequest request = RefreshTokenRequest.newBuilder()
                        .setRefreshToken(refreshToken)
                        .build();

                // Виконання запиту
                AuthResponse response = authServiceClient.refreshToken(request);

                // Збереження нових токенів
                saveAuthResponse(response);

                // Оповіщення про успішне оновлення
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Token refresh error: " + e.getMessage(), e);
                callback.onError(e.getMessage());
            }
        });
    }

    /**
     * Виконує вихід з системи
     *
     * @param callback колбек з результатом операції
     */
    public void logout(AuthCallback callback) {
        executor.execute(() -> {
            try {
                // Отримання поточного токена
                String token = tokenManager.getAccessToken();
                if (token != null) {
                    // Виконання запиту на вихід
                    authServiceClient.logout(token);
                }

                // Очищення локальних токенів
                tokenManager.clearTokens();

                // Оповіщення про успішний вихід
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Logout error: " + e.getMessage(), e);
                // Очищаємо токени локально навіть при помилці
                tokenManager.clearTokens();
                // Вважаємо вихід успішним навіть при помилці, оскільки локальні токени очищені
                callback.onSuccess();
            }
        });
    }

    /**
     * Отримує ідентифікатор пристрою або генерує новий, якщо він не існує
     *
     * @return ідентифікатор пристрою
     */
    private String getOrGenerateDeviceId() {
        // Спроба отримати збережений ідентифікатор пристрою
        String deviceId = context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
                .getString("device_id", null);

        // Якщо ідентифікатор не існує, генеруємо новий
        if (deviceId == null) {
            deviceId = SecurityUtils.generateDeviceId();
            context.getSharedPreferences("device_prefs", Context.MODE_PRIVATE)
                    .edit()
                    .putString("device_id", deviceId)
                    .apply();
        }

        return deviceId;
    }

    /**
     * Зберігає дані автентифікації
     *
     * @param response відповідь від сервера
     */
    private void saveAuthResponse(AuthResponse response) {
        // Збереження токенів
        tokenManager.saveTokens(
                response.getToken(),
                response.getRefreshToken(),
                response.getExpiresAt()
        );

        // Збереження інформації про користувача
        tokenManager.saveUserInfo(
                response.getUser().getUserId(),
                response.getUser().getUsername()
        );

        // Оновлення токена для майбутніх запитів
        authServiceClient.setAuthToken(response.getToken());
    }

    /**
     * Інтерфейс колбеку для операцій автентифікації
     */
    public interface AuthCallback {
        /**
         * Викликається при успішному виконанні операції
         */
        void onSuccess();

        /**
         * Викликається при помилці
         *
         * @param errorMessage повідомлення про помилку
         */
        void onError(String errorMessage);
    }
}