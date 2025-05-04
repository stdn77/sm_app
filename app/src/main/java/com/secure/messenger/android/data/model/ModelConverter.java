package com.secure.messenger.android.data.model;

import com.secure.messenger.android.data.local.entity.ChatGroupEntity;
import com.secure.messenger.android.data.local.entity.MessageEntity;
import com.secure.messenger.android.data.local.entity.UserEntity;
import com.secure.messenger.proto.GroupResponse;
import com.secure.messenger.proto.MessageContent;
import com.secure.messenger.proto.MessageResponse;
import com.secure.messenger.proto.MessageType;
import com.secure.messenger.proto.UserInfo;
import com.secure.messenger.proto.UserProfile;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * Утилітний клас для конвертації моделей між різними шарами додатка
 */
public class ModelConverter {

    // Конвертація користувачів

    /**
     * Конвертує UserProfile з gRPC у сутність UserEntity
     */
    public static UserEntity convertToUserEntity(UserProfile profile) {
        return new UserEntity(
                profile.getUserId(),
                profile.getUsername(),
                profile.getPhoneNumber(),
                profile.getStatus(),
                profile.getPublicKey().toByteArray(),
                convertTimestampToLocalDateTime(System.currentTimeMillis()), // Поточний час
                false // За замовчуванням не є контактом
        );
    }

    /**
     * Конвертує UserInfo з gRPC у сутність UserEntity
     */
    public static UserEntity convertToUserEntity(UserInfo userInfo) {
        LocalDateTime lastActive = userInfo.hasLastActive() ?
                convertTimestampToLocalDateTime(userInfo.getLastActive()) : null;

        return new UserEntity(
                userInfo.getId(),
                userInfo.getUsername(),
                userInfo.getPhoneNumber(),
                userInfo.getStatus(),
                userInfo.hasPublicKey() ? userInfo.getPublicKey().toByteArray() : null,
                lastActive,
                false // За замовчуванням не є контактом
        );
    }

    /**
     * Конвертує UserEntity в модель User для презентаційного шару
     */
    public static User convertToUser(UserEntity entity) {
        if (entity == null) {
            return null;
        }

        User user = new User();
        user.setId(entity.getId());
        user.setUsername(entity.getUsername());
        user.setPhoneNumber(entity.getPhoneNumber());
        user.setStatus(entity.getStatus());
        user.setPublicKey(entity.getPublicKey());
        user.setLastActive(entity.getLastActive());

        return user;
    }

    /**
     * Конвертує список UserEntity в список User
     */
    public static List<User> convertToUsers(List<UserEntity> entities) {
        List<User> users = new ArrayList<>();
        if (entities != null) {
            for (UserEntity entity : entities) {
                users.add(convertToUser(entity));
            }
        }
        return users;
    }

    // Конвертація повідомлень

    /**
     * Конвертує MessageResponse з gRPC у сутність MessageEntity
     */
    public static MessageEntity convertToMessageEntity(MessageResponse response) {
        String messageId = response.getMessageId();
        String senderId = response.getSenderId();

        // Перевірка, чи recipientId не порожній, замість використання response.hasRecipientId()
        String recipientId = !response.getRecipientId().isEmpty() ? response.getRecipientId() : null;

        // Перевірка, чи groupId не порожній, замість використання response.hasGroupId()
        String groupId = !response.getGroupId().isEmpty() ? response.getGroupId() : null;

        MessageContent content = response.getContent();
        String messageType = content.getType().name();
        byte[] encryptedContent = content.getEncryptedData().toByteArray();

        LocalDateTime createdAt = convertTimestampToLocalDateTime(response.getTimestamp());
        LocalDateTime expiresAt = createdAt.plusDays(1); // За замовчуванням повідомлення живе 1 день

        return new MessageEntity(
                messageId,
                senderId,
                recipientId,
                groupId,
                messageType,
                encryptedContent,
                createdAt,
                expiresAt,
                false, // За замовчуванням не прочитане
                true,  // За замовчуванням надіслане
                true   // За замовчуванням доставлене
        );
    }

    /**
     * Конвертує MessageEntity в модель Message для презентаційного шару
     */
    public static Message convertToMessage(MessageEntity entity) {
        if (entity == null) {
            return null;
        }

        Message message = new Message();
        message.setId(entity.getId());
        message.setSenderId(entity.getSenderId());
        message.setRecipientId(entity.getRecipientId());
        message.setGroupId(entity.getGroupId());

        // Перетворення типу повідомлення
        try {
            message.setType(Message.MessageType.valueOf(entity.getMessageType()));
        } catch (IllegalArgumentException e) {
            // За замовчуванням встановлюємо тип TEXT
            message.setType(Message.MessageType.TEXT);
        }

        message.setEncryptedContent(entity.getEncryptedContent());
        message.setCreatedAt(entity.getCreatedAt());

        // Встановлення статусу повідомлення
        if (entity.isRead()) {
            message.setStatus(Message.MessageStatus.READ);
        } else if (entity.isDelivered()) {
            message.setStatus(Message.MessageStatus.DELIVERED);
        } else if (entity.isSent()) {
            message.setStatus(Message.MessageStatus.SENT);
        } else {
            message.setStatus(Message.MessageStatus.SENDING);
        }

        return message;
    }

    /**
     * Конвертує список MessageEntity в список Message
     */
    public static List<Message> convertToMessages(List<MessageEntity> entities) {
        List<Message> messages = new ArrayList<>();
        if (entities != null) {
            for (MessageEntity entity : entities) {
                messages.add(convertToMessage(entity));
            }
        }
        return messages;
    }

    // Конвертація груп

    /**
     * Конвертує GroupResponse з gRPC у сутність ChatGroupEntity
     */
    public static ChatGroupEntity convertToChatGroupEntity(GroupResponse response) {
        return new ChatGroupEntity(
                response.getId(),
                response.getName(),
                response.getDescription(),
                response.getAdminId(),
                response.getReportEnabled(),
                response.getMemberCount(),
                null, // Шифрований ключ групи буде встановлено пізніше
                convertTimestampToLocalDateTime(response.getCreatedAt()),
                LocalDateTime.now() // Поточний час як час оновлення
        );
    }

    /**
     * Конвертує ChatGroupEntity в модель Group для презентаційного шару
     */
    public static Group convertEntityToGroup(ChatGroupEntity entity) {
        if (entity == null) {
            return null;
        }

        Group group = new Group(
                entity.getId(),
                entity.getName(),
                entity.getDescription(),
                entity.getAdminId(),
                "" // AdminUsername потрібно отримати окремо
        );

        group.setCreatedAt(entity.getCreatedAt());
        group.setMemberCount(entity.getMemberCount());
        group.setReportEnabled(entity.isReportEnabled());

        return group;
    }

    /**
     * Конвертує список ChatGroupEntity в список Group
     */
    public static List<Group> convertEntitiesToGroups(List<ChatGroupEntity> entities) {
        List<Group> groups = new ArrayList<>();
        if (entities != null) {
            for (ChatGroupEntity entity : entities) {
                groups.add(convertEntityToGroup(entity));
            }
        }
        return groups;
    }

    /**
     * Конвертує UserProfile в UserInfo
     */
    public static UserInfo convertProfileToInfo(UserProfile profile) {
        return new UserInfo(profile);
    }

    // Конвертація для gRPC запитів

    /**
     * Створює об'єкт MessageContent для відправки повідомлення
     */
    public static MessageContent createMessageContent(String textContent, MessageType type) {
        return MessageContent.newBuilder()
                .setType(type)
                .setEncryptedData(com.google.protobuf.ByteString.copyFromUtf8(textContent))
                .build();
    }

    /**
     * Створює об'єкт MessageContent для відправки повідомлення з бінарними даними
     */
    public static MessageContent createMessageContent(byte[] encryptedData, MessageType type) {
        return MessageContent.newBuilder()
                .setType(type)
                .setEncryptedData(com.google.protobuf.ByteString.copyFrom(encryptedData))
                .build();
    }

    // Допоміжні методи

    /**
     * Конвертує мітку часу (мілісекунди від епохи) в LocalDateTime
     */
    public static LocalDateTime convertTimestampToLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * Конвертує LocalDateTime в мітку часу (мілісекунди від епохи)
     */
    public static long convertLocalDateTimeToTimestamp(LocalDateTime dateTime) {
        return dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }
}