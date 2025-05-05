package com.secure.messenger.android.data.api;

import android.util.Log;

import com.secure.messenger.proto.VoiceChunk;
import com.secure.messenger.proto.VoiceMessageRequest;
import com.secure.messenger.proto.VoiceRequest;
import com.secure.messenger.proto.VoiceResponse;
import com.secure.messenger.proto.VoiceServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.grpc.stub.StreamObserver;

/**
 * Клієнт для взаємодії з сервісом голосових повідомлень gRPC
 */
public class VoiceServiceClient {
    private static final String TAG = "VoiceServiceClient";

    private final ManagedChannel channel;
    private VoiceServiceGrpc.VoiceServiceStub asyncStub;
    private VoiceServiceGrpc.VoiceServiceBlockingStub blockingStub;

    /**
     * Створює новий клієнт для сервісу голосових повідомлень
     *
     * @param serverHost хост сервера
     * @param serverPort порт сервера
     */
    public VoiceServiceClient(String serverHost, int serverPort) {
        // Ініціалізація gRPC каналу
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Для розробки, в продакшені використовуйте TLS
                .build();

        // Ініціалізація стабів
        asyncStub = VoiceServiceGrpc.newStub(channel);
        blockingStub = VoiceServiceGrpc.newBlockingStub(channel);

        Log.d(TAG, "VoiceServiceClient initialized");
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
            asyncStub = VoiceServiceGrpc.newStub(channel)
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
            blockingStub = VoiceServiceGrpc.newBlockingStub(channel)
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
            Log.d(TAG, "Auth token set for VoiceServiceClient");
        }
    }

    /**
     * Починає голосову трансляцію і повертає StreamObserver для надсилання аудіо даних
     *
     * @param targetId ідентифікатор користувача або групи
     * @param isGroup чи є це групою
     * @param responseCallback колбек для отримання відповідей
     * @return StreamObserver для надсилання аудіо даних
     */
    public StreamObserver<VoiceRequest> streamVoice(String targetId, boolean isGroup, VoiceResponseCallback responseCallback) {
        Log.d(TAG, "Starting voice stream to " + (isGroup ? "group" : "user") + ": " + targetId);

        // Створюємо стрім для отримання відповідей
        StreamObserver<VoiceResponse> responseObserver = new StreamObserver<VoiceResponse>() {
            @Override
            public void onNext(VoiceResponse response) {
                Log.d(TAG, "Received voice response from: " + response.getSenderId());
                responseCallback.onResponseReceived(response);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error in voice stream: " + t.getMessage(), t);
                responseCallback.onError(t);
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "Voice stream completed");
                responseCallback.onCompleted();
            }
        };

        // Отримуємо стрім для надсилання запитів
        return asyncStub.streamVoice(responseObserver);
    }

    /**
     * Отримує голосове повідомлення по фрагментах
     *
     * @param messageId ідентифікатор голосового повідомлення
     * @param callback колбек для отримання фрагментів голосового повідомлення
     */
    public void getVoiceMessage(String messageId, VoiceChunkCallback callback) {
        Log.d(TAG, "Getting voice message: " + messageId);

        // Створюємо запит
        VoiceMessageRequest request = VoiceMessageRequest.newBuilder()
                .setMessageId(messageId)
                .build();

        // Створюємо стрім для отримання фрагментів
        asyncStub.getVoiceMessage(request, new StreamObserver<VoiceChunk>() {
            @Override
            public void onNext(VoiceChunk chunk) {
                Log.d(TAG, "Received voice chunk: " + chunk.getSequenceNumber());
                callback.onChunkReceived(chunk);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error getting voice message: " + t.getMessage(), t);
                callback.onError(t);
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "Voice message download completed");
                callback.onCompleted();
            }
        });
    }

    /**
     * Отримує голосове повідомлення як єдиний байтовий масив (синхронно)
     *
     * @param messageId ідентифікатор голосового повідомлення
     * @return байтовий масив з аудіо даними або null у випадку помилки
     */
    public byte[] getVoiceMessageSync(String messageId) {
        Log.d(TAG, "Getting voice message synchronously: " + messageId);

        try {
            // Створюємо запит
            VoiceMessageRequest request = VoiceMessageRequest.newBuilder()
                    .setMessageId(messageId)
                    .build();

            final List<byte[]> chunks = new ArrayList<>();
            final CountDownLatch latch = new CountDownLatch(1);
            final Throwable[] error = new Throwable[1];

            // Отримуємо фрагменти голосового повідомлення
            asyncStub.getVoiceMessage(request, new StreamObserver<VoiceChunk>() {
                @Override
                public void onNext(VoiceChunk chunk) {
                    chunks.add(chunk.getChunkData().toByteArray());
                }

                @Override
                public void onError(Throwable t) {
                    error[0] = t;
                    latch.countDown();
                }

                @Override
                public void onCompleted() {
                    latch.countDown();
                }
            });

            // Чекаємо завершення отримання всіх фрагментів (з таймаутом)
            if (!latch.await(30, TimeUnit.SECONDS)) {
                Log.e(TAG, "Timeout waiting for voice message");
                return null;
            }

            // Перевіряємо, чи була помилка
            if (error[0] != null) {
                Log.e(TAG, "Error getting voice message: " + error[0].getMessage(), error[0]);
                return null;
            }

            // Об'єднуємо всі фрагменти у єдиний масив
            int totalSize = 0;
            for (byte[] chunk : chunks) {
                totalSize += chunk.length;
            }

            byte[] result = new byte[totalSize];
            int position = 0;
            for (byte[] chunk : chunks) {
                System.arraycopy(chunk, 0, result, position, chunk.length);
                position += chunk.length;
            }

            return result;
        } catch (Exception e) {
            Log.e(TAG, "Error getting voice message synchronously: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Закриває gRPC з'єднання
     */
    public void shutdown() {
        Log.d(TAG, "Shutting down VoiceServiceClient");
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    /**
     * Інтерфейс для колбеку відповідей голосової трансляції
     */
    public interface VoiceResponseCallback {
        void onResponseReceived(VoiceResponse response);
        void onError(Throwable t);
        void onCompleted();
    }

    /**
     * Інтерфейс для колбеку фрагментів голосового повідомлення
     */
    public interface VoiceChunkCallback {
        void onChunkReceived(VoiceChunk chunk);
        void onError(Throwable t);
        void onCompleted();
    }
}