package com.secure.messenger.android.data.model;

import java.time.LocalDateTime;

/**
 * Клас моделі повідомлення
 */
public class Message {

    private String id;
    private String senderId;
    private String senderName;
    private String recipientId;
    private String recipientName;
    private String groupId;
    private String groupName;
    private MessageType type;
    private byte[] encryptedContent;
    private byte[] decryptedContent;
    private MessageStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime readAt;
    private String errorMessage;
    private String localId;

    // --- Конструктори ---

    public Message() {
    }

    public Message(String id, String senderId, String recipientId, String groupId,
                   String messageType, byte[] encryptedContent,
                   LocalDateTime createdAt, LocalDateTime readAt,
                   boolean isRead, boolean isSent, boolean isDelivered) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.groupId = groupId;
        this.type = MessageType.valueOf(messageType);
        this.encryptedContent = encryptedContent;
        this.createdAt = createdAt;
        this.readAt = readAt;
        this.status = resolveStatus(isRead, isSent, isDelivered);
    }

    private MessageStatus resolveStatus(boolean isRead, boolean isSent, boolean isDelivered) {
        if (isRead) return MessageStatus.READ;
        if (isDelivered) return MessageStatus.DELIVERED;
        if (isSent) return MessageStatus.SENT;
        return MessageStatus.SENDING;
    }

    // --- Фабрики ---

    public static Message createDirectMessage(String senderId, String senderName,
                                              String recipientId, String recipientName,
                                              MessageType type, byte[] encryptedContent) {
        Message message = new Message();
        message.senderId = senderId;
        message.senderName = senderName;
        message.recipientId = recipientId;
        message.recipientName = recipientName;
        message.type = type;
        message.encryptedContent = encryptedContent;
        message.status = MessageStatus.SENDING;
        message.createdAt = LocalDateTime.now();
        return message;
    }

    public static Message createGroupMessage(String senderId, String senderName,
                                             String groupId, String groupName,
                                             MessageType type, byte[] encryptedContent) {
        Message message = new Message();
        message.senderId = senderId;
        message.senderName = senderName;
        message.groupId = groupId;
        message.groupName = groupName;
        message.type = type;
        message.encryptedContent = encryptedContent;
        message.status = MessageStatus.SENDING;
        message.createdAt = LocalDateTime.now();
        return message;
    }

    // --- Геттери / Сеттери ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getRecipientId() { return recipientId; }
    public void setRecipientId(String recipientId) { this.recipientId = recipientId; }

    public String getRecipientName() { return recipientName; }
    public void setRecipientName(String recipientName) { this.recipientName = recipientName; }

    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }

    public byte[] getEncryptedContent() { return encryptedContent; }
    public void setEncryptedContent(byte[] encryptedContent) { this.encryptedContent = encryptedContent; }

    public byte[] getDecryptedContent() { return decryptedContent; }
    public void setDecryptedContent(byte[] decryptedContent) { this.decryptedContent = decryptedContent; }

    public MessageStatus getStatus() { return status; }
    public void setStatus(MessageStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getReadAt() { return readAt; }
    public void setReadAt(LocalDateTime readAt) { this.readAt = readAt; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public String getLocalId() { return localId; }
    public void setLocalId(String localId) { this.localId = localId; }

    // --- Додаткові методи ---

    public boolean isGroupMessage() {
        return groupId != null && !groupId.isEmpty();
    }

    public boolean isFromMe(String myUserId) {
        return senderId != null && senderId.equals(myUserId);
    }

    public String getTextContent() {
        if (type == MessageType.TEXT && decryptedContent != null) {
            return new String(decryptedContent);
        }
        return null;
    }

    public String getFormattedTime() {
        if (createdAt == null) return "";
        return String.format("%02d:%02d", createdAt.getHour(), createdAt.getMinute());
    }

    // --- Внутрішні enum-и ---

    public enum MessageType {
        TEXT, IMAGE, DOCUMENT, VOICE, REPORT
    }

    public enum MessageStatus {
        SENDING, SENT, DELIVERED, READ, FAILED
    }
}
