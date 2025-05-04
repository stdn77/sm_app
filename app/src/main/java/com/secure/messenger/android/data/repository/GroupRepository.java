package com.secure.messenger.android.data.repository;

import android.content.Context;
import android.util.Log;

import com.secure.messenger.android.data.api.GroupServiceClient;
import com.secure.messenger.android.data.local.AppDatabase;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.data.local.dao.ChatGroupDao;
import com.secure.messenger.android.data.local.entity.ChatGroupEntity;
import com.secure.messenger.android.data.model.Group;
import com.secure.messenger.android.data.model.ModelConverter;
import com.secure.messenger.android.util.SecurityUtils;
import com.secure.messenger.proto.CreateGroupRequest;
import com.secure.messenger.proto.DeleteGroupRequest;
import com.secure.messenger.proto.GetGroupRequest;
import com.secure.messenger.proto.GetUserGroupsRequest;
import com.secure.messenger.proto.GroupResponse;
import com.secure.messenger.proto.GroupsResponse;
import com.secure.messenger.proto.StatusResponse;
import com.secure.messenger.proto.UpdateGroupRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;

/**
 * Репозиторій для роботи з групами чату
 */
public class GroupRepository {
    private static final String TAG = "GroupRepository";

    private final Context context;
    private final GroupServiceClient groupServiceClient;
    private final ChatGroupDao chatGroupDao;
    private final TokenManager tokenManager;
    private final SecurityUtils securityUtils;
    private final Executor executor;

    /**
     * Конструктор
     *
     * @param context            контекст додатка
     * @param groupServiceClient клієнт для сервісу груп
     */
    public GroupRepository(Context context, GroupServiceClient groupServiceClient) {
        this.context = context;
        this.groupServiceClient = groupServiceClient;
        this.chatGroupDao = AppDatabase.getInstance(context).chatGroupDao();
        this.tokenManager = new TokenManager(context);
        this.securityUtils = new SecurityUtils(context);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Створює нову групу
     *
     * @param name        назва групи
     * @param description опис групи
     * @param memberIds   список ідентифікаторів учасників
     * @param callback    колбек з результатом
     */
    public void createGroup(String name, String description, List<String> memberIds, GroupCallback callback) {
        executor.execute(() -> {
            try {
                // Аутентифікація запиту
                String token = tokenManager.getAccessToken();
                if (token == null) {
                    callback.onError("Відсутній токен авторизації");
                    return;
                }
                groupServiceClient.setAuthToken(token);

                // Створення запиту на створення групи
                CreateGroupRequest request = CreateGroupRequest.newBuilder()
                        .setName(name)
                        .setDescription(description)
                        .addAllInitialMemberIds(memberIds)
                        .build();

                // Надсилання запиту на сервер
                GroupResponse response = groupServiceClient.createGroup(request);

                // Перетворення відповіді в сутність
                ChatGroupEntity groupEntity = ModelConverter.convertToChatGroupEntity(response);

                // Генерація ключа групи
                try {
                    SecretKey groupKey = securityUtils.generateGroupKey(groupEntity.getId());
                    // Збереження зашифрованого ключа
                    groupEntity.setEncryptedGroupKey(groupKey.getEncoded());
                } catch (Exception e) {
                    Log.e(TAG, "Error generating group key: " + e.getMessage(), e);
                }

                // Збереження групи локально
                chatGroupDao.insert(groupEntity);

                // Створення моделі групи для відповіді
                Group group = ModelConverter.convertEntityToGroup(groupEntity);
                callback.onSuccess(group);
            } catch (Exception e) {
                Log.e(TAG, "Error creating group: " + e.getMessage(), e);
                callback.onError("Помилка при створенні групи: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує групу за ідентифікатором
     *
     * @param groupId  ідентифікатор групи
     * @param callback колбек з результатом
     */
    public void getGroupById(String groupId, GroupCallback callback) {
        executor.execute(() -> {
            try {
                // Спочатку перевіряємо локальну базу даних
                ChatGroupEntity localGroup = chatGroupDao.getGroupById(groupId);

                if (localGroup != null) {
                    Group group = ModelConverter.convertEntityToGroup(localGroup);
                    callback.onSuccess(group);
                } else {
                    // Якщо локально немає, запитуємо з сервера
                    fetchGroupFromServer(groupId, callback);
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting group by ID: " + e.getMessage(), e);
                callback.onError("Помилка при отриманні групи: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує групу з сервера
     *
     * @param groupId  ідентифікатор групи
     * @param callback колбек з результатом
     */
    private void fetchGroupFromServer(String groupId, GroupCallback callback) {
        try {
            // Аутентифікація запиту
            String token = tokenManager.getAccessToken();
            if (token == null) {
                callback.onError("Відсутній токен авторизації");
                return;
            }
            groupServiceClient.setAuthToken(token);

            // Створення запиту на отримання групи
            GetGroupRequest request = GetGroupRequest.newBuilder()
                    .setGroupId(groupId)
                    .build();

            // Надсилання запиту на сервер
            GroupResponse response = groupServiceClient.getGroup(request);

            // Перетворення відповіді в сутність
            ChatGroupEntity groupEntity = ModelConverter.convertToChatGroupEntity(response);

            // Збереження групи локально
            chatGroupDao.insert(groupEntity);

            // Створення моделі групи для відповіді
            Group group = ModelConverter.convertEntityToGroup(groupEntity);
            callback.onSuccess(group);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching group from server: " + e.getMessage(), e);
            callback.onError("Помилка при отриманні групи з сервера: " + e.getMessage());
        }
    }

    /**
     * Отримує всі групи користувача
     *
     * @param callback колбек зі списком груп
     */
    public void getUserGroups(GroupsCallback callback) {
        executor.execute(() -> {
            try {
                String userId = tokenManager.getUserId();
                if (userId == null) {
                    callback.onError("Відсутній ідентифікатор користувача");
                    return;
                }

                // Спочатку отримуємо групи з локальної бази даних
                List<ChatGroupEntity> localGroups = chatGroupDao.getAllGroups();

                if (!localGroups.isEmpty()) {
                    List<Group> groups = ModelConverter.convertEntitiesToGroups(localGroups);
                    callback.onSuccess(groups);
                }

                // Потім оновлюємо з сервера
                fetchUserGroupsFromServer(userId, callback);
            } catch (Exception e) {
                Log.e(TAG, "Error getting user groups: " + e.getMessage(), e);
                callback.onError("Помилка при отриманні груп користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує групи користувача з сервера
     *
     * @param userId   ідентифікатор користувача
     * @param callback колбек зі списком груп
     */
    private void fetchUserGroupsFromServer(String userId, GroupsCallback callback) {
        try {
            // Аутентифікація запиту
            String token = tokenManager.getAccessToken();
            if (token == null) {
                callback.onError("Відсутній токен авторизації");
                return;
            }
            groupServiceClient.setAuthToken(token);

            // Створення запиту на отримання груп користувача
            GetUserGroupsRequest request = GetUserGroupsRequest.newBuilder()
                    .setUserId(userId)
                    .build();

            // Надсилання запиту на сервер
            GroupsResponse response = groupServiceClient.getUserGroups(request);

            // Перетворення відповідей в сутності та збереження їх локально
            List<ChatGroupEntity> groupEntities = new ArrayList<>();
            for (GroupResponse groupResponse : response.getGroupsList()) {
                ChatGroupEntity groupEntity = ModelConverter.convertToChatGroupEntity(groupResponse);
                groupEntities.add(groupEntity);
                chatGroupDao.insert(groupEntity);
            }

            // Створення моделей груп для відповіді
            List<Group> groups = ModelConverter.convertEntitiesToGroups(groupEntities);
            callback.onSuccess(groups);
        } catch (Exception e) {
            Log.e(TAG, "Error fetching user groups from server: " + e.getMessage(), e);
            callback.onError("Помилка при отриманні груп з сервера: " + e.getMessage());
        }
    }

    /**
     * Оновлює інформацію про групу
     *
     * @param groupId     ідентифікатор групи
     * @param name        нова назва групи
     * @param description новий опис групи
     * @param callback    колбек з результатом
     */
    public void updateGroup(String groupId, String name, String description, OperationCallback callback) {
        executor.execute(() -> {
            try {
                // Аутентифікація запиту
                String token = tokenManager.getAccessToken();
                if (token == null) {
                    callback.onError("Відсутній токен авторизації");
                    return;
                }
                groupServiceClient.setAuthToken(token);

                // Створення запиту на оновлення групи
                UpdateGroupRequest request = UpdateGroupRequest.newBuilder()
                        .setGroupId(groupId)
                        .setName(name)
                        .setDescription(description)
                        .build();

                // Надсилання запиту на сервер
                GroupResponse response = groupServiceClient.updateGroup(request);

                // Перетворення відповіді в сутність
                ChatGroupEntity groupEntity = ModelConverter.convertToChatGroupEntity(response);

                // Оновлення групи локально
                groupEntity.setUpdatedAt(LocalDateTime.now());
                chatGroupDao.update(groupEntity);

                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error updating group: " + e.getMessage(), e);
                callback.onError("Помилка при оновленні групи: " + e.getMessage());
            }
        });
    }

    /**
     * Видаляє групу
     *
     * @param groupId  ідентифікатор групи
     * @param callback колбек з результатом
     */
    public void deleteGroup(String groupId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                // Аутентифікація запиту
                String token = tokenManager.getAccessToken();
                if (token == null) {
                    callback.onError("Відсутній токен авторизації");
                    return;
                }
                groupServiceClient.setAuthToken(token);

                // Створення запиту на видалення групи
                DeleteGroupRequest request = DeleteGroupRequest.newBuilder()
                        .setGroupId(groupId)
                        .build();

                // Надсилання запиту на сервер
                StatusResponse response = groupServiceClient.deleteGroup(request);

                if (response.getSuccess()) {
                    // Видалення групи локально
                    ChatGroupEntity group = chatGroupDao.getGroupById(groupId);
                    if (group != null) {
                        chatGroupDao.delete(group);
                    }

                    // Видалення ключа групи з кешу
                    securityUtils.removeGroupKey(groupId);

                    callback.onSuccess();
                } else {
                    callback.onError("Помилка при видаленні групи: " + response.getMessage());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting group: " + e.getMessage(), e);
                callback.onError("Помилка при видаленні групи: " + e.getMessage());
            }
        });
    }

    /**
     * Увімкнення/вимкнення звітів для групи
     *
     * @param groupId  ідентифікатор групи
     * @param enabled  статус увімкнення
     * @param callback колбек з результатом
     */
    public void setReportEnabled(String groupId, boolean enabled, OperationCallback callback) {
        executor.execute(() -> {
            try {
                // Оновлення звітів локально
                chatGroupDao.setReportEnabled(groupId, enabled);

                // TODO: Реалізувати надсилання запиту на сервер для оновлення налаштувань звітів

                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error setting report enabled: " + e.getMessage(), e);
                callback.onError("Помилка при налаштуванні звітів: " + e.getMessage());
            }
        });
    }

    /**
     * Оновлення ключа групи
     *
     * @param groupId      ідентифікатор групи
     * @param encryptedKey зашифрований ключ
     * @param callback     колбек з результатом
     */
    public void updateGroupKey(String groupId, byte[] encryptedKey, OperationCallback callback) {
        executor.execute(() -> {
            try {
                // Оновлення ключа групи локально
                chatGroupDao.updateEncryptedGroupKey(groupId, encryptedKey);

                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error updating group key: " + e.getMessage(), e);
                callback.onError("Помилка при оновленні ключа групи: " + e.getMessage());
            }
        });
    }

    /**
     * Колбек для операцій з однією групою
     */
    public interface GroupCallback {
        void onSuccess(Group group);

        void onError(String error);
    }

    /**
     * Колбек для операцій зі списком груп
     */
    public interface GroupsCallback {
        void onSuccess(List<Group> groups);

        void onError(String error);
    }

    /**
     * Колбек для загальних операцій
     */
    public interface OperationCallback {
        void onSuccess();

        void onError(String error);
    }
}