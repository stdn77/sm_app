package com.secure.messenger.android.data.api;

import android.util.Log;

import com.secure.messenger.proto.AuthServiceGrpc;
import com.secure.messenger.proto.DeleteMessageRequest;
import com.secure.messenger.proto.MarkAsReadRequest;
import com.secure.messenger.proto.MessageRequest;
import com.secure.messenger.proto.MessageResponse;
import com.secure.messenger.proto.MessageServiceGrpc;
import com.secure.messenger.proto.ReceiveRequest;
import com.secure.messenger.proto.StatusResponse;

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
 * Клієнт для взаємодії з сервісом повідомлень gRPC
 */
public class MessageServiceClient {
    private static final String TAG = "MessageServiceClient";

    private final ManagedChannel channel;
    private MessageServiceGrpc.MessageServiceStub asyncStub;
    private MessageServiceGrpc.MessageServiceBlockingStub blockingStub;

    /**
     * Створює новий клієнт для сервісу повідомлень
     *
     * @param serverHost хост сервера
     * @param serverPort порт сервера
     */
    public MessageServiceClient(String serverHost, int serverPort) {
        // Ініціалізація gRPC каналу
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Для розробки, в продакшені використовуйте TLS
                .build();

        // Ініціалізація стабів
        asyncStub = MessageServiceGrpc.newStub(channel);
        blockingStub = MessageServiceGrpc.newBlockingStub(channel);

        Log.d(TAG, "MessageServiceClient initialized");
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
            blockingStub = MessageServiceGrpc.newBlockingStub(channel)
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
            asyncStub = MessageServiceGrpc.newStub(channel)
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));
            Log.d(TAG, "Auth token set for MessageServiceClient");
        }
    }

    /**
     * Надсилає повідомлення
     *
     * @param request запит з повідомленням
     * @param callback колбек із результатом
     */
    public void sendMessage(MessageRequest request, final StatusCallback callback) {
        Log.d(TAG, "Sending message to: " +
                (!request.getRecipientId().isEmpty() ? request.getRecipientId() : request.getGroupId()));

        // Створюємо стрім для відправки повідомлення
        StreamObserver<StatusResponse> responseObserver = new StreamObserver<StatusResponse>() {
            @Override
            public void onNext(StatusResponse response) {
                Log.d(TAG, "Message sent, status: " + response.getSuccess());
                callback.onResponse(response);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error sending message: " + t.getMessage(), t);
                callback.onError(t);
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "Send message completed");
            }
        };

        // Отримуємо стрімовий обсервер для відправки повідомлень
        StreamObserver<MessageRequest> requestObserver = asyncStub.sendMessage(responseObserver);

        try {
            // Надсилаємо повідомлення
            requestObserver.onNext(request);
            requestObserver.onCompleted();
        } catch (Exception e) {
            Log.e(TAG, "Error sending message request: " + e.getMessage(), e);
            requestObserver.onError(e);
            callback.onError(e);
        }
    }

    /**
     * Отримує повідомлення з сервера
     *
     * @param request запит на отримання повідомлень
     * @param callback колбек для обробки отриманих повідомлень
     */
    public void receiveMessages(ReceiveRequest request, final MessageResponseCallback callback) {
        Log.d(TAG, "Receiving messages since: " + request.getSinceTimestamp());

        // Створюємо стрім для отримання повідомлень
        StreamObserver<MessageResponse> responseObserver = new StreamObserver<MessageResponse>() {
            @Override
            public void onNext(MessageResponse response) {
                Log.d(TAG, "Received message: " + response.getMessageId());
                callback.onMessageReceived(response);
            }

            @Override
            public void onError(Throwable t) {
                Log.e(TAG, "Error receiving messages: " + t.getMessage(), t);
                callback.onError(t);
            }

            @Override
            public void onCompleted() {
                Log.d(TAG, "Receive messages completed");
                callback.onCompleted();
            }
        };

        // Надсилаємо запит на отримання повідомлень
        asyncStub.receiveMessages(request, responseObserver);
    }

    /**
     * Позначає повідомлення як прочитані
     *
     * @param messageIds список ідентифікаторів повідомлень
     * @param callback колбек із результатом
     */
    public void markAsRead(List<String> messageIds, final StatusCallback callback) {
        MarkAsReadRequest request = MarkAsReadRequest.newBuilder()
                .addAllMessageIds(messageIds)
                .build();

        Log.d(TAG, "Marking messages as read: " + messageIds);

        try {
            // Виклик блокуючого методу без StreamObserver
            StatusResponse response = blockingStub.markAsRead(request);
            callback.onResponse(response);
        } catch (Exception e) {
            Log.e(TAG, "Error marking messages as read: " + e.getMessage(), e);
            callback.onError(e);
        }
    }

    /**
     * Видаляє повідомлення
     *
     * @param messageId ідентифікатор повідомлення
     * @param callback колбек із результатом
     */
    public void deleteMessage(String messageId, final StatusCallback callback) {
        DeleteMessageRequest request = DeleteMessageRequest.newBuilder()
                .setMessageId(messageId)
                .build();

        Log.d(TAG, "Deleting message: " + messageId);

        try {
            // Виклик блокуючого методу без StreamObserver
            StatusResponse response = blockingStub.deleteMessage(request);
            callback.onResponse(response);
        } catch (Exception e) {
            Log.e(TAG, "Error deleting message: " + e.getMessage(), e);
            callback.onError(e);
        }
    }

    /**
     * Закриває gRPC з'єднання
     */
    public void shutdown() {
        Log.d(TAG, "Shutting down MessageServiceClient");
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }

    /**
     * Інтерфейс для колбеку відповіді про статус
     */
    public interface StatusCallback {
        void onResponse(StatusResponse response);
        void onError(Throwable t);
    }

    /**
     * Інтерфейс для колбеку отримання повідомлень
     */
    public interface MessageResponseCallback {
        void onMessageReceived(MessageResponse message);
        void onCompleted();
        void onError(Throwable t);
    }
}