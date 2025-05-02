package com.secure.messenger.android.data.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Модель для відображення превью чату в списку чатів
 */
public class ChatPreview {
    private final String id;
    private final String name;
    private final String lastMessage;
    private final LocalDateTime lastMessageTime;
    private final int unreadCount;
    private final boolean isGroup;
    private final boolean isOnline;
    private final String avatarUrl;

    /**
     * Конструктор
     *
     * @param id              ідентифікатор чату (ID користувача або групи)
     * @param name            назва чату (ім'я користувача або назва групи)
     * @param lastMessage     останнє повідомлення
     * @param lastMessageTime час останнього повідомлення
     * @param unreadCount     кількість непрочитаних повідомлень
     * @param isGroup         чи є це груповий чат
     * @param isOnline        чи онлайн користувач (тільки для приватних чатів)
     * @param avatarUrl       URL аватара
     */
    public ChatPreview(String id, String name, String lastMessage, LocalDateTime lastMessageTime,
                       int unreadCount, boolean isGroup, boolean isOnline, String avatarUrl) {
        this.id = id;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.unreadCount = unreadCount;
        this.isGroup = isGroup;
        this.isOnline = isOnline;
        this.avatarUrl = avatarUrl;
    }

    /**
     * Отримує ідентифікатор чату
     *
     * @return ідентифікатор чату
     */
    public String getId() {
        return id;
    }

    /**
     * Отримує назву чату
     *
     * @return назва чату
     */
    public String getName() {
        return name;
    }

    /**
     * Отримує останнє повідомлення
     *
     * @return останнє повідомлення
     */
    public String getLastMessage() {
        return lastMessage;
    }

    /**
     * Отримує час останнього повідомлення
     *
     * @return час останнього повідомлення
     */
    public LocalDateTime getLastMessageTime() {
        return lastMessageTime;
    }

    /**
     * Отримує кількість непрочитаних повідомлень
     *
     * @return кількість непрочитаних повідомлень
     */
    public int getUnreadCount() {
        return unreadCount;
    }

    /**
     * Перевіряє, чи є це груповий чат
     *
     * @return true, якщо це груповий чат
     */
    public boolean isGroup() {
        return isGroup;
    }

    /**
     * Перевіряє, чи онлайн користувач (тільки для приватних чатів)
     *
     * @return true, якщо користувач онлайн
     */
    public boolean isOnline() {
        return isOnline;
    }

    /**
     * Отримує URL аватара
     *
     * @return URL аватара
     */
    public String getAvatarUrl() {
        return avatarUrl;
    }

    /**
     * Отримує форматований час останнього повідомлення
     *
     * @return форматований час
     */
    public String getFormattedTime() {
        if (lastMessageTime == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter;

        // Вибираємо формат залежно від часу
        if (lastMessageTime.toLocalDate().equals(now.toLocalDate())) {
            // Якщо сьогодні - показуємо час
            formatter = DateTimeFormatter.ofPattern("HH:mm");
        } else if (ChronoUnit.DAYS.between(lastMessageTime, now) < 7) {
            // Якщо менше тижня - показуємо день тижня
            formatter = DateTimeFormatter.ofPattern("EEE");
        } else {
            // Інакше - повну дату
            formatter = DateTimeFormatter.ofPattern("dd.MM.yy");
        }

        return lastMessageTime.format(formatter);
    }

    /**
     * Перевіряє, чи є непрочитані повідомлення
     *
     * @return true, якщо є непрочитані повідомлення
     */
    public boolean hasUnreadMessages() {
        return unreadCount > 0;
    }

    /**
     * Отримує ініціали для відображення аватара
     *
     * @return ініціали
     */
    public String getInitials() {
        if (name == null || name.isEmpty()) {
            return isGroup ? "G" : "?";
        }

        String[] parts = name.split("\\s+");
        StringBuilder initials = new StringBuilder();

        for (String part : parts) {
            if (!part.isEmpty()) {
                initials.append(part.charAt(0));
                if (initials.length() >= 2) {
                    break;
                }
            }
        }

        return initials.toString().toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ChatPreview that = (ChatPreview) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}