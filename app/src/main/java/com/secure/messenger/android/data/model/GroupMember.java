package com.secure.messenger.android.data.model;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Клас моделі учасника групи
 */
public class GroupMember {

    // Ідентифікатор запису учасника групи
    private String id;

    // Ідентифікатор користувача
    private String userId;

    // Ідентифікатор групи
    private String groupId;

    // Ім'я користувача
    private String username;

    // Роль користувача у групі
    private Role role;

    // Зашифрований ключ групи для цього користувача
    private byte[] encryptedKey;

    // Час приєднання до групи
    private LocalDateTime joinedAt;

    /**
     * Конструктор за замовчуванням
     */
    public GroupMember() {
    }

    /**
     * Конструктор з параметрами
     *
     * @param userId ідентифікатор користувача
     * @param groupId ідентифікатор групи
     * @param username ім'я користувача
     * @param role роль у групі
     */
    public GroupMember(String userId, String groupId, String username, Role role) {
        this.userId = userId;
        this.groupId = groupId;
        this.username = username;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    /**
     * @return ідентифікатор запису
     */
    public String getId() {
        return id;
    }

    /**
     * @param id ідентифікатор запису
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return ідентифікатор користувача
     */
    public String getUserId() {
        return userId;
    }

    /**
     * @param userId ідентифікатор користувача
     */
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * @return ідентифікатор групи
     */
    public String getGroupId() {
        return groupId;
    }

    /**
     * @param groupId ідентифікатор групи
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
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
     * @return роль у групі
     */
    public Role getRole() {
        return role;
    }

    /**
     * @param role роль у групі
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * @return зашифрований ключ групи
     */
    public byte[] getEncryptedKey() {
        return encryptedKey;
    }

    /**
     * @param encryptedKey зашифрований ключ групи
     */
    public void setEncryptedKey(byte[] encryptedKey) {
        this.encryptedKey = encryptedKey;
    }

    /**
     * @return час приєднання до групи
     */
    public LocalDateTime getJoinedAt() {
        return joinedAt;
    }

    /**
     * @param joinedAt час приєднання до групи
     */
    public void setJoinedAt(LocalDateTime joinedAt) {
        this.joinedAt = joinedAt;
    }

    /**
     * Перевіряє, чи має учасник адміністративні права
     *
     * @return true, якщо учасник має адміністративні права
     */
    public boolean hasAdminPrivileges() {
        return role == Role.ADMIN || role == Role.MODERATOR;
    }

    /**
     * @return перша літера імені користувача (для відображення у аватарі)
     */
    public char getFirstLetter() {
        if (username != null && !username.isEmpty()) {
            return username.charAt(0);
        }
        return 'U';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GroupMember that = (GroupMember) o;
        return Objects.equals(userId, that.userId) && Objects.equals(groupId, that.groupId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, groupId);
    }

    /**
     * Перелік можливих ролей у групі
     */
    public enum Role {
        /**
         * Адміністратор групи - має повні права
         */
        ADMIN,

        /**
         * Модератор - може керувати учасниками, але не може видалити групу
         */
        MODERATOR,

        /**
         * Звичайний учасник - може тільки спілкуватися
         */
        MEMBER
    }
}