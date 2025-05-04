package com.secure.messenger.android.data.repository;

import android.content.Context;
import android.util.Log;

import com.secure.messenger.android.data.api.MessageServiceClient;
import com.secure.messenger.android.data.local.AppDatabase;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.data.local.dao.MessageDao;
import com.secure.messenger.android.data.local.entity.MessageEntity;
import com.secure.messenger.android.util.SecurityUtils;
import com.secure.messenger.proto.MessageContent;
import com.secure.messenger.proto.MessageRequest;
import com.secure.messenger.proto.MessageResponse;
import com.secure.messenger.proto.MessageType;
import com.secure.messenger.proto.ReceiveRequest;

import com.google.protobuf.ByteString;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.crypto.SecretKey;

/**
 * Репозиторій для роботи з повідомленнями
 */
public class MessageRepository {
    private static final String TAG = "MessageRepository";

    private final MessageServiceClient messageServiceClient;
    private final TokenManager tokenManager;
    private final MessageDao messageDao;
    private final Executor executor;
    private final Context context;
    private final SecurityUtils securityUtils;

    /**
     * Створює новий екземпляр репозиторію повідомлень
     *
     * @param context контекст додатка
     * @param messageServiceClient клієнт для сервісу повідомлень
     */
    public MessageRepository(Context context, MessageServiceClient messageServiceClient) {
        this.context = context;
        this.messageServiceClient = messageServiceClient;
        this.tokenManager = new TokenManager(context);
        this.messageDao = AppDatabase.getInstance(context).messageDao();
        this.executor = Executors.newSingleThreadExecutor();
        this.securityUtils = new SecurityUtils(context);
    }

    /**
     * Відправляє приватне повідомлення
     *
     * @param recipientId ідентифікатор отримувача
     * @param content вміст повідомлення
     * @param messageType тип повідомлення
     * @param callback колбек з результатом операції
     */
    public void sendDirectMessage(String recipientId, String content, String messageType, MessageCallback callback) {
        executor.execute(() -> {
            try {
                // Генеруємо ID повідомлення
                String messageId = UUID.randomUUID().toString();

                // Шифруємо повідомлення
                byte[] encryptedContent = encryptContentForUser(recipientId, content.getBytes());

                // Створюємо запит на відправку повідомлення
                MessageRequest request = createMessageRequest(recipientId, null, encryptedContent, getMessageType(messageType));

                // Зберігаємо повідомлення локально
                MessageEntity messageEntity = createLocalMessage(
                        messageId,
                        tokenManager.getUserId(),
                        recipientId,
                        null,
                        messageType,
                        encryptedContent,
                        false,
                        true,
                        false
                );

                messageDao.insert(messageEntity);

                // Відправляємо повідомлення на сервер
                messageServiceClient.sendMessage(request, response -> {
                    if (response.getSuccess()) {
                        // Позначаємо повідомлення як надіслане
                        messageDao.markAsSent(messageId);
                        callback.onSuccess(messageId);
                    } else {
                        callback.onError("Failed to send message: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error sending direct message: " + e.getMessage(), e);
                callback.onError("Error sending message: " + e.getMessage());
            }
        });
    }

    /**
     * Відправляє групове повідомлення
     *
     * @param groupId ідентифікатор групи
     * @param content вміст повідомлення
     * @param messageType тип повідомлення
     * @param callback колбек з результатом операції
     */
    public void sendGroupMessage(String groupId, String content, String messageType, MessageCallback callback) {
        executor.execute(() -> {
            try {
                // Генеруємо ID повідомлення
                String messageId = UUID.randomUUID().toString();

                // Шифруємо повідомлення ключем групи
                byte[] encryptedContent = encryptContentForGroup(groupId, content.getBytes());

                // Створюємо запит на відправку повідомлення
                MessageRequest request = createMessageRequest(null, groupId, encryptedContent, getMessageType(messageType));

                // Зберігаємо повідомлення локально
                MessageEntity messageEntity = createLocalMessage(
                        messageId,
                        tokenManager.getUserId(),
                        null,
                        groupId,
                        messageType,
                        encryptedContent,
                        false,
                        true,
                        false
                );

                messageDao.insert(messageEntity);

                // Відправляємо повідомлення на сервер
                messageServiceClient.sendMessage(request, response -> {
                    if (response.getSuccess()) {
                        // Позначаємо повідомлення як надіслане
                        messageDao.markAsSent(messageId);
                        callback.onSuccess(messageId);
                    } else {
                        callback.onError("Failed to send message: " + response.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error sending group message: " + e.getMessage(), e);
                callback.onError("Error sending message: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує повідомлення з сервера, оновлює локальну базу даних та повертає повідомлення
     *
     * @param sinceTimestamp час останнього оновлення
     * @param callback колбек з результатом операції
     */
    public void fetchMessages(long sinceTimestamp, FetchMessagesCallback callback) {
        executor.execute(() -> {
            try {
                // Створюємо запит на отримання повідомлень
                ReceiveRequest request = ReceiveRequest.newBuilder()
                        .setSinceTimestamp(sinceTimestamp)
                        .setLimit(100) // Обмежуємо кількість повідомлень
                        .build();

                List<MessageEntity> messageEntities = new ArrayList<>();

                // Отримуємо повідомлення від сервера через стрімінг
                messageServiceClient.receiveMessages(request, new MessageServiceClient.MessageResponseCallback() {
                    @Override
                    public void onMessageReceived(MessageResponse message) {
                        try {
                            // Перетворюємо отримане повідомлення у локальну сутність
                            MessageEntity entity = mapMessageResponseToEntity(message);
                            messageEntities.add(entity);

                            // Зберігаємо повідомлення локально
                            messageDao.insert(entity);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing received message: " + e.getMessage(), e);
                        }
                    }

                    @Override
                    public void onCompleted() {
                        callback.onSuccess(messageEntities);
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "Error receiving messages: " + t.getMessage(), t);
                        callback.onError("Error receiving messages: " + t.getMessage());
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error fetching messages: " + e.getMessage(), e);
                callback.onError("Error fetching messages: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує особисті повідомлення між поточним користувачем та іншим користувачем
     *
     * @param otherUserId ідентифікатор іншого користувача
     * @param callback колбек з результатом операції
     */
    public void getDirectMessages(String otherUserId, FetchMessagesCallback callback) {
        executor.execute(() -> {
            try {
                String currentUserId = tokenManager.getUserId();
                List<MessageEntity> messages = messageDao.getMessagesBetweenUsers(currentUserId, otherUserId);
                callback.onSuccess(messages);
            } catch (Exception e) {
                Log.e(TAG, "Error getting direct messages: " + e.getMessage(), e);
                callback.onError("Error getting direct messages: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує групові повідомлення
     *
     * @param groupId ідентифікатор групи
     * @param callback колбек з результатом операції
     */
    public void getGroupMessages(String groupId, FetchMessagesCallback callback) {
        executor.execute(() -> {
            try {
                List<MessageEntity> messages = messageDao.getGroupMessages(groupId);
                callback.onSuccess(messages);
            } catch (Exception e) {
                Log.e(TAG, "Error getting group messages: " + e.getMessage(), e);
                callback.onError("Error getting group messages: " + e.getMessage());
            }
        });
    }

    /**
     * Позначає повідомлення як прочитане
     *
     * @param messageId ідентифікатор повідомлення
     * @param callback колбек з результатом операції
     */
    public void markMessageAsRead(String messageId, MessageCallback callback) {
        executor.execute(() -> {
            try {
                // Позначаємо повідомлення як прочитане локально
                messageDao.markAsRead(messageId);

                // Відправляємо запит на сервер для позначення повідомлення як прочитане
                // TODO: Реалізувати відправку запиту на сервер

                callback.onSuccess(messageId);
            } catch (Exception e) {
                Log.e(TAG, "Error marking message as read: " + e.getMessage(), e);
                callback.onError("Error marking message as read: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує всі непрочитані повідомлення для поточного користувача
     *
     * @param callback колбек з результатом операції
     */
    public void getUnreadMessages(FetchMessagesCallback callback) {
        executor.execute(() -> {
            try {
                String userId = tokenManager.getUserId();
                List<MessageEntity> messages = messageDao.getUnreadMessagesForUser(userId);
                callback.onSuccess(messages);
            } catch (Exception e) {
                Log.e(TAG, "Error getting unread messages: " + e.getMessage(), e);
                callback.onError("Error getting unread messages: " + e.getMessage());
            }
        });
    }

    /**
     * Видаляє повідомлення
     *
     * @param messageId ідентифікатор повідомлення
     * @param callback колбек з результатом операції
     */
    public void deleteMessage(String messageId, MessageCallback callback) {
        executor.execute(() -> {
            try {
                // Отримуємо повідомлення з бази даних
                MessageEntity message = messageDao.getMessageById(messageId);
                if (message == null) {
                    callback.onError("Message not found");
                    return;
                }

                // Видаляємо повідомлення з бази даних
                messageDao.delete(message);

                // Відправляємо запит на видалення повідомлення на сервер
                // TODO: Реалізувати відправку запиту на сервер

                callback.onSuccess(messageId);
            } catch (Exception e) {
                Log.e(TAG, "Error deleting message: " + e.getMessage(), e);
                callback.onError("Error deleting message: " + e.getMessage());
            }
        });
    }

    // Допоміжні методи

    /**
     * Шифрує вміст для конкретного користувача
     */
    private byte[] encryptContentForUser(String userId, byte[] content) {
        try {
            // В реальній імплементації тут треба отримати публічний ключ користувача
            // та виконати шифрування використовуючи SecurityUtils

            // Для простоти використовуємо симетричне AES-шифрування
            SecretKey secretKey = SecurityUtils.generateAESKey();
            return SecurityUtils.encryptWithAES(content, secretKey);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting content for user: " + e.getMessage(), e);
            throw new RuntimeException("Error encrypting content: " + e.getMessage());
        }
    }

    /**
     * Шифрує вміст для групи
     */
    private byte[] encryptContentForGroup(String groupId, byte[] content) {
        try {
            // В реальній імплементації тут треба отримати ключ групи
            // з securityUtils та виконати шифрування

            // Отримуємо ключ групи
            SecretKey groupKey = securityUtils.getGroupKey(groupId);

            // Якщо ключ не знайдено, генеруємо новий
            if (groupKey == null) {
                groupKey = securityUtils.generateGroupKey(groupId);
            }

            // Шифруємо зміст
            return SecurityUtils.encryptWithAES(content, groupKey);
        } catch (Exception e) {
            Log.e(TAG, "Error encrypting content for group: " + e.getMessage(), e);
            throw new RuntimeException("Error encrypting content: " + e.getMessage());
        }
    }

    /**
     * Створює запит на відправку повідомлення
     */
    private MessageRequest createMessageRequest(String recipientId, String groupId, byte[] encryptedContent, MessageType messageType) {
        MessageRequest.Builder requestBuilder = MessageRequest.newBuilder();

        // Встановлюємо отримувача або групу
        if (recipientId != null) {
            requestBuilder.setRecipientId(recipientId);
        }

        if (groupId != null) {
            requestBuilder.setGroupId(groupId);
        }

        // Встановлюємо тип та зашифрований вміст повідомлення
        MessageContent content = MessageContent.newBuilder()
                .setType(messageType)
                .setEncryptedData(ByteString.copyFrom(encryptedContent))
                .build();

        requestBuilder.setContent(content);

        return requestBuilder.build();
    }

    /**
     * Створює локальне повідомлення
     */
    private MessageEntity createLocalMessage(String messageId, String senderId, String recipientId,
                                             String groupId, String messageType, byte[] encryptedContent,
                                             boolean isRead, boolean isSent, boolean isDelivered) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusDays(1); // Повідомлення протримається 1 день

        return new MessageEntity(
                messageId,
                senderId,
                recipientId,
                groupId,
                messageType,
                encryptedContent,
                now,
                expiresAt,
                isRead,
                isSent,
                isDelivered
        );
    }

    /**
     * Перетворює тип повідомлення з рядка на enum
     */
    private MessageType getMessageType(String messageType) {
        switch (messageType.toUpperCase()) {
            case "TEXT":
                return MessageType.TEXT;
            case "IMAGE":
                return MessageType.IMAGE;
            case "DOCUMENT":
                return MessageType.DOCUMENT;
            case "VOICE":
                return MessageType.VOICE;
            case "REPORT":
                return MessageType.REPORT;
            default:
                return MessageType.TEXT;
        }
    }

    /**
     * Перетворює відповідь сервера на локальну сутність
     */
    private MessageEntity mapMessageResponseToEntity(MessageResponse response) {
        String messageId = response.getMessageId();
        String senderId = response.getSenderId();
        String recipientId = response.hasRecipientId() ? response.getRecipientId() : null;
        String groupId = response.hasGroupId() ? response.getGroupId() : null;
        String messageType = response.getContent().getType().name();
        byte[] encryptedContent = response.getContent().getEncryptedData().toByteArray();

        LocalDateTime createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(response.getTimestamp()), ZoneId.systemDefault());

        LocalDateTime expiresAt = response.hasExpiresAt() ?
                LocalDateTime.ofInstant(Instant.ofEpochMilli(response.getExpiresAt()), ZoneId.systemDefault()) :
                createdAt.plusDays(1);

        // За замовчуванням, повідомлення не прочитане, але відправлене та доставлене
        boolean isRead = false;
        boolean isSent = true;
        boolean isDelivered = true;

        return new MessageEntity(
                messageId,
                senderId,
                recipientId,
                groupId,
                messageType,
                encryptedContent,
                createdAt,
                expiresAt,
                isRead,
                isSent,
                isDelivered
        );
    }

    /**
     * Інтерфейс колбеку для операцій з повідомленнями
     */
    public interface MessageCallback {
        void onSuccess(String messageId);
        void onError(String errorMessage);
    }

    /**
     * Інтерфейс колбеку для отримання повідомлень
     */
    public interface FetchMessagesCallback {
        void onSuccess(List<MessageEntity> messages);
        void onError(String errorMessage);
    }
}