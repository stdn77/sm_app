package com.secure.messenger.android.data.api;

import android.util.Log;

import com.secure.messenger.proto.CreateGroupRequest;
import com.secure.messenger.proto.DeleteGroupRequest;
import com.secure.messenger.proto.GetGroupRequest;
import com.secure.messenger.proto.GetUserGroupsRequest;
import com.secure.messenger.proto.GroupResponse;
import com.secure.messenger.proto.GroupServiceGrpc;
import com.secure.messenger.proto.GroupsResponse;
import com.secure.messenger.proto.StatusResponse;
import com.secure.messenger.proto.UpdateGroupRequest;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;

/**
 * Клієнт для взаємодії з сервісом груп gRPC
 */
public class GroupServiceClient {
    private static final String TAG = "GroupServiceClient";

    private final ManagedChannel channel;
    private GroupServiceGrpc.GroupServiceBlockingStub blockingStub;

    /**
     * Створює новий клієнт для сервісу груп
     *
     * @param serverHost хост сервера
     * @param serverPort порт сервера
     */
    public GroupServiceClient(String serverHost, int serverPort) {
        // Ініціалізація gRPC каналу
        channel = ManagedChannelBuilder.forAddress(serverHost, serverPort)
                .usePlaintext() // Для розробки, в продакшені використовуйте TLS
                .build();

        // Ініціалізація стабу
        blockingStub = GroupServiceGrpc.newBlockingStub(channel);

        Log.d(TAG, "GroupServiceClient initialized");
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

            // Оновлюємо стаб з новими метаданими
            blockingStub = GroupServiceGrpc.newBlockingStub(channel)
                    .withInterceptors(MetadataUtils.newAttachHeadersInterceptor(metadata));


            Log.d(TAG, "Auth token set for GroupServiceClient");
        }
    }

    /**
     * Створює нову групу
     *
     * @param request запит на створення групи
     * @return відповідь з інформацією про групу
     */
    public GroupResponse createGroup(CreateGroupRequest request) {
        Log.d(TAG, "Creating group with name: " + request.getName());
        return blockingStub.createGroup(request);
    }

    /**
     * Отримує інформацію про групу
     *
     * @param request запит на отримання групи
     * @return відповідь з інформацією про групу
     */
    public GroupResponse getGroup(GetGroupRequest request) {
        Log.d(TAG, "Getting group with ID: " + request.getGroupId());
        return blockingStub.getGroup(request);
    }

    /**
     * Отримує список груп користувача
     *
     * @param request запит на отримання груп користувача
     * @return відповідь зі списком груп
     */
    public GroupsResponse getUserGroups(GetUserGroupsRequest request) {
        Log.d(TAG, "Getting groups for user: " + request.getUserId());
        return blockingStub.getUserGroups(request);
    }

    /**
     * Оновлює інформацію про групу
     *
     * @param request запит на оновлення групи
     * @return відповідь з оновленою інформацією про групу
     */
    public GroupResponse updateGroup(UpdateGroupRequest request) {
        Log.d(TAG, "Updating group with ID: " + request.getGroupId());
        return blockingStub.updateGroup(request);
    }

    /**
     * Видаляє групу
     *
     * @param request запит на видалення групи
     * @return відповідь зі статусом операції
     */
    public StatusResponse deleteGroup(DeleteGroupRequest request) {
        Log.d(TAG, "Deleting group with ID: " + request.getGroupId());
        return blockingStub.deleteGroup(request);
    }

    /**
     * Закриває gRPC з'єднання
     */
    public void shutdown() {
        Log.d(TAG, "Shutting down GroupServiceClient");
        if (channel != null && !channel.isShutdown()) {
            channel.shutdown();
        }
    }
}