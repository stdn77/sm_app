package com.secure.messenger.android.data.api;

import android.content.Context;
import android.util.Log;

import com.secure.messenger.android.data.local.KeyValueStorage;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import io.grpc.CallCredentials;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.Status;
import io.grpc.stub.MetadataUtils;

/**
 * Базовий клас для роботи з gRPC сервером.
 * Забезпечує підключення до сервера, надання авторизаційних даних та створення стабів.
 */
public class GrpcClient {
    private static final String TAG = "GrpcClient";
    private static final String SERVER_HOST = "10.0.2.2"; // localhost для емулятора Android
    private static final int SERVER_PORT = 9090;
    private static final int TIMEOUT_SECONDS = 10;

    private ManagedChannel channel;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final KeyValueStorage keyValueStorage;

    /**
     * Конструктор
     * @param context контекст додатка для доступу до сховища авторизаційних даних
     */
    public GrpcClient(Context context) {
        this.keyValueStorage = new KeyValueStorage(context);
        setupChannel();
    }

    /**
     * Налаштовує канал для з'єднання з gRPC сервером
     */
    private void setupChannel() {
        channel = ManagedChannelBuilder.forAddress(SERVER_HOST, SERVER_PORT)
                .usePlaintext() // Для розробки. В продакшені використовувати TLS!
                .build();
    }

    /**
     * Отримує авторизаційний токен з локального сховища і
     * створює об'єкт MetadataUtils для додавання заголовків авторизації
     *
     * @return метадані з авторизаційними заголовками або null, якщо токен відсутній
     */
    protected io.grpc.ClientInterceptor getAuthInterceptor() {
        String token = keyValueStorage.getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.d(TAG, "Не знайдено токен авторизації");
            return null;
        }

        Metadata metadata = new Metadata();
        Metadata.Key<String> authKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        metadata.put(authKey, "Bearer " + token);

        return MetadataUtils.newAttachHeadersInterceptor(metadata);
    }

    /**
     * Створює авторизаційні дані для захищених запитів
     *
     * @return об'єкт CallCredentials з авторизаційними даними
     */
    protected CallCredentials getCallCredentials() {
        return new CallCredentials() {
            @Override
            public void applyRequestMetadata(RequestInfo requestInfo, Executor appExecutor, MetadataApplier applier) {
                appExecutor.execute(() -> {
                    try {
                        Metadata headers = new Metadata();
                        String token = keyValueStorage.getAuthToken();
                        if (token != null && !token.isEmpty()) {
                            Metadata.Key<String> authKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
                            headers.put(authKey, "Bearer " + token);
                            applier.apply(headers);
                        } else {
                            applier.fail(Status.fromThrowable(new IllegalStateException("Немає авторизаційного токена")));
                        }
                    } catch (Throwable e) {
                        applier.fail(Status.fromThrowable(e));
                    }
                });
            }

            @Override
            public void thisUsesUnstableApi() {
                // Цей метод потрібен для попередження про нестабільність API
            }
        };
    }

    /**
     * Закриває з'єднання з gRPC сервером
     */
    public void shutdown() {
        try {
            channel.shutdown().awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(TAG, "Помилка закриття каналу: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Отримує канал для створення стабів
     *
     * @return канал для gRPC комунікації
     */
    protected ManagedChannel getChannel() {
        return channel;
    }

    /**
     * Отримує executor для асинхронних операцій
     *
     * @return executor
     */
    protected Executor getExecutor() {
        return executor;
    }
}