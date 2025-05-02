package com.secure.messenger.proto;

/**
 * Клас для зберігання інформації про користувача.
 * Примітка: Цей клас створений вручну, оскільки він не є частиною прото-файлу.
 */
public class UserInfo {

    private String id;
    private String username;
    private String phoneNumber;
    private String status;
    private com.google.protobuf.ByteString publicKey;
    private long lastActive;
    private boolean isOnline;

    /**
     * Конструктор за замовчуванням
     */
    public UserInfo() {
        id = "";
        username = "";
        phoneNumber = "";
        status = "";
        publicKey = com.google.protobuf.ByteString.EMPTY;
        lastActive = 0L;
        isOnline = false;
    }

    /**
     * Створює UserInfo з UserProfile
     *
     * @param profile Профіль користувача
     */
    public UserInfo(UserProfile profile) {
        this.id = profile.getUserId();
        this.username = profile.getUsername();
        this.phoneNumber = profile.getPhoneNumber();
        this.status = profile.getStatus();
        this.publicKey = profile.getPublicKey();
        this.lastActive = 0L; // Не зберігається в UserProfile
        this.isOnline = false; // Не зберігається в UserProfile
    }

    /**
     * @return ID користувача
     */
    public String getId() {
        return id;
    }

    /**
     * @param id ID користувача
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return Ім'я користувача
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username Ім'я користувача
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return Номер телефону
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * @param phoneNumber Номер телефону
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * @return Статус користувача
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status Статус користувача
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @return Публічний ключ
     */
    public com.google.protobuf.ByteString getPublicKey() {
        return publicKey;
    }

    /**
     * @param publicKey Публічний ключ
     */
    public void setPublicKey(com.google.protobuf.ByteString publicKey) {
        this.publicKey = publicKey;
    }

    /**
     * @return Час останньої активності
     */
    public long getLastActive() {
        return lastActive;
    }

    /**
     * @param lastActive Час останньої активності
     */
    public void setLastActive(long lastActive) {
        this.lastActive = lastActive;
    }

    /**
     * @return Чи користувач онлайн
     */
    public boolean getIsOnline() {
        return isOnline;
    }

    /**
     * @param isOnline Чи користувач онлайн
     */
    public void setIsOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    /**
     * Перевіряє, чи встановлено значення lastActive
     */
    public boolean hasLastActive() {
        return lastActive > 0;
    }

    /**
     * Перевіряє, чи встановлено публічний ключ
     */
    public boolean hasPublicKey() {
        return publicKey != null && !publicKey.isEmpty();
    }

    /**
     * Створює Builder для UserInfo
     */
    public static Builder newBuilder() {
        return new Builder();
    }

    /**
     * Builder для UserInfo
     */
    public static class Builder {
        private UserInfo userInfo;

        private Builder() {
            userInfo = new UserInfo();
        }

        /**
         * @param id ID користувача
         */
        public Builder setId(String id) {
            userInfo.id = id;
            return this;
        }

        /**
         * @param username Ім'я користувача
         */
        public Builder setUsername(String username) {
            userInfo.username = username;
            return this;
        }

        /**
         * @param phoneNumber Номер телефону
         */
        public Builder setPhoneNumber(String phoneNumber) {
            userInfo.phoneNumber = phoneNumber;
            return this;
        }

        /**
         * @param status Статус користувача
         */
        public Builder setStatus(String status) {
            userInfo.status = status;
            return this;
        }

        /**
         * @param publicKey Публічний ключ
         */
        public Builder setPublicKey(com.google.protobuf.ByteString publicKey) {
            userInfo.publicKey = publicKey;
            return this;
        }

        /**
         * @param lastActive Час останньої активності
         */
        public Builder setLastActive(long lastActive) {
            userInfo.lastActive = lastActive;
            return this;
        }

        /**
         * @param isOnline Чи користувач онлайн
         */
        public Builder setIsOnline(boolean isOnline) {
            userInfo.isOnline = isOnline;
            return this;
        }

        /**
         * @return Побудований об'єкт UserInfo
         */
        public UserInfo build() {
            return userInfo;
        }
    }
}