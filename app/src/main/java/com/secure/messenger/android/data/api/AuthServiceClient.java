package com.secure.messenger.android.data.api;

import android.util.Log;

import com.secure.messenger.proto.AuthResponse;
import com.secure.messenger.proto.AuthServiceGrpc;
import com.secure.messenger.proto.LoginRequest;
import com.secure.messenger.proto.LogoutRequest;
import com.secure.messenger.proto.RefreshTokenRequest;
import com.secure.messenger.proto.RegisterRequest;
import com.secure.messenger.proto.StatusResponse;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

/**
 * Клієнт для взаємодії з сервісом автентифікації gRPC
 */
public class AuthServiceClient {
    private static final String TAG = "AuthServiceClient";

    private final ManagedChannel channel;
    private AuthServiceGrpc.AuthServiceBlockingStub blockingStub;
    private AuthServiceGrpc.AuthServiceStub asyncStub;

    /**
     * Створює новий клієнт для сервісу автентифікації
     *
     * @param serverHost хост сервера
     * @param serverPort порт сервера
     */
    public AuthServiceClient(String serverHost, int serverPort) {
        // Ініціалізація gRPC каналу
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Для розробки, в продакшені використовуйте TLS
                .build();

        // Ініціалізація стабів
        blockingStub = AuthServiceGrpc.newBlockingStub(channel);
        asyncStub = AuthServiceGrpc.newStub(channel);

        Log.d(TAG, "AuthServiceClient initialized");
    }

    /**
     * Встановлює токен автентифікації для запитів
     *
     * @param token токен автентифікації
     */
    public void setAuthToken(String token) {
        if (token != null && !token.isEmpty()) {
            // Додаємо токен до метаданих запитів
            Metadata metadata = new Metadata();
            Metadata.Key<String> key = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
            metadata.put(key, "Bearer " + token);

            // Оновлюємо стаби з новими метаданими
            blockingStub = MetadataUtils.attachHeaders(AuthServiceGrpc.newBlockingStub(channel), metadata);
            asyncStub = MetadataUtils.attachHeaders(AuthServiceGrpc.newStub(channel), metadata);

            Log.d(TAG, "Auth token set for gRPC client");
        }
    }

    /**
     * Реєстрація нового користувача
     *
     * @param request запит на реєстрацію
     * @return відповідь від сервера
     */
    public AuthResponse register(RegisterRequest request) {
        Log.d(TAG, "Registering user: " + request.getUsername());
        return blockingStub.register(request);
    }

    /**
     * Вхід користувача
     *
     * @param request запит на вхід
     * @return відповідь від сервера
     */
    public AuthResponse login(LoginRequest request) {
        Log.d(TAG, "Logging in user: " + request.getUsername());
        return blockingStub.login(request);
    }

    /**
     * Оновлення токена
     *
     * @param request запит на оновлення токена
     * @return відповідь від сервера
     */
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        Log.d(TAG, "Refreshing token");
        return blockingStub.refreshToken(request);
    }

    /**
     * Вихід користувача
     *
     * @param token токен для виходу
     * @return відповідь від сервера
     */
    public StatusResponse logout(String token) {
        Log.d(TAG, "Logging out");
        LogoutRequest request = LogoutRequest.newBuilder()
                .setToken(token)
                .build();
        return blockingStub.logout(request);
    }

    /**
     * Закриває gRPC з'єднання
     */
    public void shutdown() {
        Log.d(TAG, "Shutting down AuthServiceClient");
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}