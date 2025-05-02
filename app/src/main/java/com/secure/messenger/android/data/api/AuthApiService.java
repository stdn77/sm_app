package com.secure.messenger.android.data.api;

import android.content.Context;
import android.util.Log;

import com.google.protobuf.ByteString;
import com.secure.messenger.android.data.local.KeyValueStorage;
import com.secure.messenger.android.data.model.AuthResult;
import com.secure.messenger.android.data.model.User;
import com.secure.messenger.proto.AuthResponse;
import com.secure.messenger.proto.AuthServiceGrpc;
import com.secure.messenger.proto.LoginRequest;
import com.secure.messenger.proto.LogoutRequest;
import com.secure.messenger.proto.RefreshTokenRequest;
import com.secure.messenger.proto.RegisterRequest;
import com.secure.messenger.proto.UserProfile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.grpc.Status;
import io.grpc.StatusRuntimeException;

/**
 * Сервіс для автентифікації та авторизації через gRPC API
 */
public class AuthApiService extends GrpcClient {
    private static final String TAG = "AuthApiService";
    private final AuthServiceGrpc.AuthServiceBlockingStub blockingStub;
    private final KeyValueStorage keyValueStorage;

    /**
     * Конструктор
     * @param context контекст додатка
     */
    public AuthApiService(Context context) {
        super(context);
        blockingStub = AuthServiceGrpc.newBlockingStub(getChannel())
                .withDeadlineAfter(30, TimeUnit.SECONDS);
        keyValueStorage = new KeyValueStorage(context);
    }

    /**
     * Реєструє нового користувача
     *
     * @param username ім'я користувача
     * @param phoneNumber номер телефону
     * @param password пароль
     * @param publicKey публічний ключ
     * @return результат реєстрації з токеном або помилкою
     */
    public AuthResult register(String username, String phoneNumber, String password, byte[] publicKey) {
        try {
            String deviceId = keyValueStorage.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
                keyValueStorage.saveDeviceId(deviceId);
            }

            RegisterRequest request = RegisterRequest.newBuilder()
                    .setUsername(username)
                    .setPhoneNumber(phoneNumber)
                    .setPassword(password)
                    .setPublicKey(ByteString.copyFrom(publicKey))
                    .setDeviceId(ByteString.copyFrom(deviceId.getBytes()))
                    .setDeviceName(android.os.Build.MODEL)
                    .build();

            AuthResponse response = blockingStub.register(request);
            saveAuthData(response);
            return new AuthResult(true, null);
        } catch (StatusRuntimeException e) {
            Log.e(TAG, "Помилка при реєстрації: " + e.getMessage());
            String errorMessage = extractErrorMessage(e);
            return new AuthResult(false, errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Неочікувана помилка при реєстрації: " + e.getMessage());
            return new AuthResult(false, "Неочікувана помилка: " + e.getMessage());
        }
    }

    /**
     * Виконує вхід користувача
     *
     * @param username ім'я користувача
     * @param password пароль
     * @return результат входу з токеном або помилкою
     */
    public AuthResult login(String username, String password) {
        try {
            String deviceId = keyValueStorage.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                deviceId = UUID.randomUUID().toString();
                keyValueStorage.saveDeviceId(deviceId);
            }

            LoginRequest request = LoginRequest.newBuilder()
                    .setUsername(username)
                    .setPassword(password)
                    .setDeviceId(ByteString.copyFrom(deviceId.getBytes()))
                    .setDeviceName(android.os.Build.MODEL)
                    .build();

            AuthResponse response = blockingStub.login(request);
            saveAuthData(response);
            return new AuthResult(true, null);
        } catch (StatusRuntimeException e) {
            Log.e(TAG, "Помилка при вході: " + e.getMessage());
            String errorMessage = extractErrorMessage(e);
            return new AuthResult(false, errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Неочікувана помилка при вході: " + e.getMessage());
            return new AuthResult(false, "Неочікувана помилка: " + e.getMessage());
        }
    }

    /**
     * Оновлює токен авторизації
     *
     * @return результат оновлення з новим токеном або помилкою
     */
    public AuthResult refreshToken() {
        try {
            String refreshToken = keyValueStorage.getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                return new AuthResult(false, "Немає токена для оновлення");
            }

            RefreshTokenRequest request = RefreshTokenRequest.newBuilder()
                    .setRefreshToken(refreshToken)
                    .build();

            AuthResponse response = blockingStub.refreshToken(request);
            saveAuthData(response);
            return new AuthResult(true, null);
        } catch (StatusRuntimeException e) {
            Log.e(TAG, "Помилка при оновленні токена: " + e.getMessage());
            String errorMessage = extractErrorMessage(e);
            return new AuthResult(false, errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Неочікувана помилка при оновленні токена: " + e.getMessage());
            return new AuthResult(false, "Неочікувана помилка: " + e.getMessage());
        }
    }

    /**
     * Виконує вихід з системи
     *
     * @return результат виходу або помилка
     */
    public AuthResult logout() {
        try {
            String token = keyValueStorage.getAuthToken();
            if (token == null || token.isEmpty()) {
                keyValueStorage.clearAuthData();
                return new AuthResult(true, null);
            }

            LogoutRequest request = LogoutRequest.newBuilder()
                    .setToken(token)
                    .build();

            // Додаємо авторизаційні метадані до запиту
            blockingStub.withCallCredentials(getCallCredentials()).logout(request);

            // Очищаємо локальні авторизаційні дані
            keyValueStorage.clearAuthData();
            return new AuthResult(true, null);
        } catch (StatusRuntimeException e) {
            Log.e(TAG, "Помилка при виході: " + e.getMessage());
            String errorMessage = extractErrorMessage(e);

            // При помилці все одно очищаємо токен
            keyValueStorage.clearAuthData();

            return new AuthResult(false, errorMessage);
        } catch (Exception e) {
            Log.e(TAG, "Неочікувана помилка при виході: " + e.getMessage());

            // При помилці все одно очищаємо токен
            keyValueStorage.clearAuthData();

            return new AuthResult(false, "Неочікувана помилка: " + e.getMessage());
        }
    }

    /**
     * Перевіряє, чи є користувач авторизованим
     *
     * @return true, якщо токен авторизації присутній і не протермінований
     */
    public boolean isAuthenticated() {
        // Перевіряємо наявність токена
        String token = keyValueStorage.getAuthToken();
        if (token == null || token.isEmpty()) {
            return false;
        }

        // Перевіряємо термін дії токена
        long expirationTime = keyValueStorage.getTokenExpirationTime();
        return expirationTime > System.currentTimeMillis();
    }

    /**
     * Отримує поточного авторизованого користувача
     *
     * @return об'єкт користувача або null, якщо не авторизований
     */
    public User getCurrentUser() {
        if (!isAuthenticated()) {
            return null;
        }

        String userId = keyValueStorage.getUserId();
        String username = keyValueStorage.getUsername();
        String phoneNumber = keyValueStorage.getUserPhoneNumber();
        byte[] publicKey = keyValueStorage.getUserPublicKey();
        String status = keyValueStorage.getUserStatus();

        if (userId == null || username == null) {
            return null;
        }

        User user = new User();
        user.setId(userId);
        user.setUsername(username);
        user.setPhoneNumber(phoneNumber);
        user.setPublicKey(publicKey);
        user.setStatus(status);

        return user;
    }

    /**
     * Зберігає дані авторизації в локальне сховище
     *
     * @param response відповідь від сервера з авторизаційними даними
     */
    private void saveAuthData(AuthResponse response) {
        keyValueStorage.saveAuthToken(response.getToken());
        keyValueStorage.saveRefreshToken(response.getRefreshToken());
        keyValueStorage.saveTokenExpirationTime(response.getExpiresAt());

        UserProfile userProfile = response.getUser();
        if (userProfile != null) {
            keyValueStorage.saveUserId(userProfile.getUserId());
            keyValueStorage.saveUsername(userProfile.getUsername());
            keyValueStorage.saveUserPhoneNumber(userProfile.getPhoneNumber());
            keyValueStorage.saveUserPublicKey(userProfile.getPublicKey().toByteArray());
            keyValueStorage.saveUserStatus(userProfile.getStatus());
        }
    }

    /**
     * Витягує осмислене повідомлення про помилку з gRPC винятку
     *
     * @param e виняток
     * @return повідомлення про помилку
     */
    private String extractErrorMessage(StatusRuntimeException e) {
        Status status = e.getStatus();
        if (status.getCode() == Status.Code.UNAUTHENTICATED) {
            return "Помилка автентифікації";
        } else if (status.getCode() == Status.Code.INVALID_ARGUMENT) {
            return "Неправильні дані: " + status.getDescription();
        } else if (status.getCode() == Status.Code.ALREADY_EXISTS) {
            return "Користувач вже існує";
        } else if (status.getCode() == Status.Code.PERMISSION_DENIED) {
            return "Доступ заборонено";
        } else if (status.getCode() == Status.Code.UNAVAILABLE) {
            return "Сервер недоступний. Перевірте з'єднання з інтернетом";
        } else {
            return status.getDescription() != null ? status.getDescription() : "Помилка: " + status.getCode();
        }
    }

    /**
     * Конвертує мітку часу (мілісекунди від епохи) в LocalDateTime
     */
    private LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }
}