package com.secure.messenger.android.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.time.LocalDateTime;

/**
 * Сутність для зберігання інформації про користувачів у локальній базі даних
 */
@Entity(tableName = "users")
public class UserEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String username;

    private String phoneNumber;

    private String status;

    private byte[] publicKey;

    private LocalDateTime lastActive;

    private boolean isContact;

    /**
     * Стандартний конструктор
     */
    public UserEntity() {
    }

    /**
     * Конструктор з параметрами
     *
     * @param id ідентифікатор користувача
     * @param username ім'я користувача
     * @param phoneNumber номер телефону
     * @param status статус
     * @param publicKey публічний ключ
     * @param lastActive час останньої активності
     * @param isContact чи є користувач контактом
     */
    public UserEntity(@NonNull String id, String username, String phoneNumber, String status,
                      byte[] publicKey, LocalDateTime lastActive, boolean isContact) {
        this.id = id;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.publicKey = publicKey;
        this.lastActive = lastActive;
        this.isContact = isContact;
    }

    @NonNull
    public String getId() {
        return id;
    }

    public void setId(@NonNull String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    public LocalDateTime getLastActive() {
        return lastActive;
    }

    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    public boolean isContact() {
        return isContact;
    }

    public void setContact(boolean contact) {
        isContact = contact;
    }
}