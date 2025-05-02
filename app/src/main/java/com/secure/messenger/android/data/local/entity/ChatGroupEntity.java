package com.secure.messenger.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

/**
 * Сутність для зберігання груп чату в локальній базі даних
 */
@Entity(
        tableName = "chat_groups",
        foreignKeys = {
                @ForeignKey(
                        entity = UserEntity.class,
                        parentColumns = "id",
                        childColumns = "adminId",
                        onDelete = ForeignKey.CASCADE
                )
        },
        indices = {
                @Index("adminId")
        }
)
public class ChatGroupEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;

    private String description;

    private String adminId;

    private boolean reportEnabled;

    private int memberCount;

    private byte[] encryptedGroupKey;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    /**
     * Стандартний конструктор
     */
    public ChatGroupEntity() {
    }

    /**
     * Конструктор з параметрами
     */
    public ChatGroupEntity(@NonNull String id, String name, String description, String adminId,
                           boolean reportEnabled, int memberCount, byte[] encryptedGroupKey,
                           LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.adminId = adminId;
        this.reportEnabled = reportEnabled;
        this.memberCount = memberCount;
        this.encryptedGroupKey = encryptedGroupKey;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAdminId() {
        return adminId;
    }

    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    public boolean isReportEnabled() {
        return reportEnabled;
    }

    public void setReportEnabled(boolean reportEnabled) {
        this.reportEnabled = reportEnabled;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public byte[] getEncryptedGroupKey() {
        return encryptedGroupKey;
    }

    public void setEncryptedGroupKey(byte[] encryptedGroupKey) {
        this.encryptedGroupKey = encryptedGroupKey;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}