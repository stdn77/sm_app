package com.secure.messenger.android.data.model;

import java.time.LocalDateTime;

/**
 * Клас моделі користувача
 */
public class User {

    // Ідентифікатор користувача
    private String id;

    // Ім'я користувача
    private String username;

    // Номер телефону користувача
    private String phoneNumber;

    // Публічний ключ користувача для шифрування повідомлень
    private byte[] publicKey;

    // Статус користувача (ACTIVE, INACTIVE, BLOCKED)
    private String status;

    // Час останньої активності
    private LocalDateTime lastActive;

    // Прапорець "онлайн"
    private boolean online;

    /**
     * Порожній конструктор
     */
    public User() {
    }

    /**
     * Конструктор з параметрами
     *
     * @param id ідентифікатор користувача
     * @param username ім'я користувача
     * @param phoneNumber номер телефону користувача
     * @param publicKey публічний ключ користувача
     * @param status статус користувача
     */
    public User(String id, String username, String phoneNumber, byte[] publicKey, String status) {
        this.id = id;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.publicKey = publicKey;
        this.status = status;
    }

    /**
     * Конструктор з додатковими параметрами
     *
     * @param id ідентифікатор користувача
     * @param username ім'я користувача
     * @param phoneNumber номер телефону
     * @param status статус користувача
     * @param publicKey публічний ключ користувача
     * @param lastActive час останньої активності
     * @param isContact чи є користувач контактом
     */
    public User(String id, String username, String phoneNumber, String status, byte[] publicKey, LocalDateTime lastActive, boolean isContact) {
        this.id = id;
        this.username = username;
        this.phoneNumber = phoneNumber;
        this.status = status;
        this.publicKey = publicKey;
        this.lastActive = lastActive;
        this.online = false; // За замовчуванням не онлайн
    }


    /**
     * @return ідентифікатор користувача
     */
    public String getId() {
        return id;
    }

    /**
     * @param id ідентифікатор користувача
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return ім'я користувача
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username ім'я користувача
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return номер телефону користувача
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber номер телефону користувача
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return публічний ключ користувача
     */
    public byte[] getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey публічний ключ користувача
     */
    public void setPublicKey(byte[] publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return статус користувача
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status статус користувача
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return час останньої активності
     */
    public LocalDateTime getLastActive() {
        return lastActive;
    }

    /**
     * @param lastActive час останньої активності
     */
    public void setLastActive(LocalDateTime lastActive) {
        this.lastActive = lastActive;
    }

    /**
     * @return користувач онлайн?
     */
    public boolean isOnline() {
        return online;
    }

    /**
     * @param online користувач онлайн?
     */
    public void setOnline(boolean online) {
        this.online = online;
    }

    /**
     * @return маскований номер телефону для відображення
     */
    public String getMaskedPhoneNumber() {
        if (phoneNumber == null || phoneNumber.length() <= 4) {
            return "****";
        }
        return "*".repeat(phoneNumber.length() - 4) + phoneNumber.substring(phoneNumber.length() - 4);
    }

    /**
     * Перевіряє, чи є користувач активним
     *
     * @return true, якщо користувач активний
     */
    public boolean isActive() {
        return "ACTIVE".equals(status);
    }
}