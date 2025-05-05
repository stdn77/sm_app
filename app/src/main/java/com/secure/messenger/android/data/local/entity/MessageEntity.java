package com.secure.messenger.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

/**
 * Сутність для зберігання повідомлень у локальній базі даних
 */
@Entity(
        tableName = "messages",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "senderId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "recipientId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = ChatGroupEntity.class,
                        parentColumns = "id",
                        childColumns = "groupId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("senderId"),
                @Index("recipientId"),
                @Index("groupId")
        }
)
public class MessageEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String senderId;

    private String recipientId;

    private String groupId;

    private String messageType;

    private byte[] encryptedContent;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private boolean isRead;

    private boolean isSent;

    private boolean isDelivered;

    /**
     * Стандартний конструктор
     */
    public MessageEntity() {
    }

    /**
     * Конструктор з параметрами
     */
    @Ignore
    public MessageEntity(@NonNull String id, String senderId, String recipientId, String groupId,
                         String messageType, byte[] encryptedContent, LocalDateTime createdAt,
                         LocalDateTime expiresAt, boolean isRead, boolean isSent, boolean isDelivered) {
        this.id = id;
        this.senderId = senderId;
        this.recipientId = recipientId;
        this.groupId = groupId;
        this.messageType = messageType;
        this.encryptedContent = encryptedContent;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.isRead = isRead;
        this.isSent = isSent;
        this.isDelivered = isDelivered;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public byte[] getEncryptedContent() {
        return encryptedContent;
    }

    public void setEncryptedContent(byte[] encryptedContent) {
        this.encryptedContent = encryptedContent;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public boolean isSent() {
        return isSent;
    }

    public void setSent(boolean sent) {
        isSent = sent;
    }

    public boolean isDelivered() {
        return isDelivered;
    }

    public void setDelivered(boolean delivered) {
        isDelivered = delivered;
    }
}