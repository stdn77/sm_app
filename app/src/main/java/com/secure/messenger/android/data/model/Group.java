package com.secure.messenger.android.data.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Клас моделі групи
 */
public class Group {

    // Ідентифікатор групи
    private String id;

    // Назва групи
    private String name;

    // Опис групи
    private String description;

    // Ідентифікатор адміністратора групи
    private String adminId;

    // Ім'я адміністратора групи
    private String adminUsername;

    // Кількість учасників у групі
    private int memberCount;

    // Чи включено функцію звітів у групі
    private boolean reportEnabled;

    // Час створення групи
    private LocalDateTime createdAt;

    // Список учасників групи
    private List<GroupMember> members = new ArrayList<>();

    // Останнє повідомлення у групі
    private Message lastMessage;

    // Кількість непрочитаних повідомлень у групі
    private int unreadCount;

    /**
     * Конструктор за замовчуванням
     */
    public Group() {
    }

    /**
     * Конструктор з параметрами
     *
     * @param id ідентифікатор групи
     * @param name назва групи
     * @param description опис групи
     * @param adminId ідентифікатор адміністратора
     * @param adminUsername ім'я адміністратора
     */
    public Group(String id, String name, String description, String adminId, String adminUsername) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.adminId = adminId;
        this.adminUsername = adminUsername;
    }

    /**
     * @return ідентифікатор групи
     */
    public String getId() {
        return id;
    }

    /**
     * @param id ідентифікатор групи
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * @return назва групи
     */
    public String getName() {
        return name;
    }

    /**
     * @param name назва групи
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return опис групи
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description опис групи
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return ідентифікатор адміністратора
     */
    public String getAdminId() {
        return adminId;
    }

    /**
     * @param adminId ідентифікатор адміністратора
     */
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    /**
     * @return ім'я адміністратора
     */
    public String getAdminUsername() {
        return adminUsername;
    }

    /**
     * @param adminUsername ім'я адміністратора
     */
    public void setAdminUsername(String adminUsername) {
        this.adminUsername = adminUsername;
    }

    /**
     * @return кількість учасників групи
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * @param memberCount кількість учасників групи
     */
    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    /**
     * @return чи включено функцію звітів
     */
    public boolean isReportEnabled() {
        return reportEnabled;
    }

    /**
     * @param reportEnabled чи включено функцію звітів
     */
    public void setReportEnabled(boolean reportEnabled) {
        this.reportEnabled = reportEnabled;
    }

    /**
     * @return час створення групи
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * @param createdAt час створення групи
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * @return список учасників групи
     */
    public List<GroupMember> getMembers() {
        return members;
    }

    /**
     * @param members список учасників групи
     */
    public void setMembers(List<GroupMember> members) {
        this.members = members;
    }

    /**
     * Додає учасника до групи
     *
     * @param member новий учасник
     */
    public void addMember(GroupMember member) {
        if (!members.contains(member)) {
            members.add(member);
            memberCount = members.size();
        }
    }

    /**
     * Видаляє учасника з групи
     *
     * @param userId ідентифікатор користувача для видалення
     * @return true, якщо учасник був видалений
     */
    public boolean removeMember(String userId) {
        boolean removed = members.removeIf(member -> member.getUserId().equals(userId));
        if (removed) {
            memberCount = members.size();
        }
        return removed;
    }

    /**
     * @return останнє повідомлення у групі
     */
    public Message getLastMessage() {
        return lastMessage;
    }

    /**
     * @param lastMessage останнє повідомлення у групі
     */
    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    /**
     * @return кількість непрочитаних повідомлень
     */
    public int getUnreadCount() {
        return unreadCount;
    }

    /**
     * @param unreadCount кількість непрочитаних повідомлень
     */
    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    /**
     * Перевіряє, чи є вказаний користувач адміністратором групи
     *
     * @param userId ідентифікатор користувача
     * @return true, якщо користувач є адміністратором
     */
    public boolean isAdmin(String userId) {
        return adminId != null && adminId.equals(userId);
    }

    /**
     * Перевіряє, чи є вказаний користувач учасником групи
     *
     * @param userId ідентифікатор користувача
     * @return true, якщо користувач є учасником
     */
    public boolean isMember(String userId) {
        return members.stream().anyMatch(member -> member.getUserId().equals(userId));
    }

    /**
     * Отримує роль користувача в групі
     *
     * @param userId ідентифікатор користувача
     * @return роль користувача або null, якщо користувач не є учасником
     */
    public GroupMember.Role getUserRole(String userId) {
        return members.stream()
                .filter(member -> member.getUserId().equals(userId))
                .map(GroupMember::getRole)
                .findFirst()
                .orElse(null);
    }

    /**
     * @return перша літера назви групи (для відображення у аватарі)
     */
    public char getFirstLetter() {
        if (name != null && !name.isEmpty()) {
            return name.charAt(0);
        }
        return 'G';
    }
}