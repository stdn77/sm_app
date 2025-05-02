package com.secure.messenger.android.data.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * Модель групи чату для презентаційного шару
 */
public class ChatGroup {
    private final String id;
    private final String name;
    private final String description;
    private final String adminId;
    private final boolean reportEnabled;
    private final int memberCount;
    private final byte[] encryptedGroupKey;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    /**
     * Створює нову групу чату
     *
     * @param id ідентифікатор
     * @param name назва групи
     * @param description опис групи
     * @param adminId ідентифікатор адміністратора
     * @param reportEnabled чи активовані доповіді
     * @param memberCount кількість учасників
     * @param encryptedGroupKey зашифрований ключ групи
     * @param createdAt час створення
     * @param updatedAt час останнього оновлення
     */
    public ChatGroup(String id, String name, String description, String adminId,
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

    /**
     * Отримує ідентифікатор групи
     *
     * @return ідентифікатор
     */
    public String getId() {
        return id;
    }

    /**
     * Отримує назву групи
     *
     * @return назва
     */
    public String getName() {
        return name;
    }

    /**
     * Отримує опис групи
     *
     * @return опис
     */
    public String getDescription() {
        return description;
    }

    /**
     * Отримує ідентифікатор адміністратора
     *
     * @return ідентифікатор адміністратора
     */
    public String getAdminId() {
        return adminId;
    }

    /**
     * Перевіряє, чи активовані доповіді
     *
     * @return true, якщо доповіді активовані
     */
    public boolean isReportEnabled() {
        return reportEnabled;
    }

    /**
     * Отримує кількість учасників групи
     *
     * @return кількість учасників
     */
    public int getMemberCount() {
        return memberCount;
    }

    /**
     * Отримує зашифрований ключ групи
     *
     * @return зашифрований ключ
     */
    public byte[] getEncryptedGroupKey() {
        return encryptedGroupKey;
    }

    /**
     * Отримує час створення групи
     *
     * @return час створення
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Отримує час останнього оновлення групи
     *
     * @return час оновлення
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Перевіряє, чи є користувач адміністратором групи
     *
     * @param userId ідентифікатор користувача
     * @return true, якщо користувач є адміністратором
     */
    public boolean isAdmin(String userId) {
        return adminId != null && adminId.equals(userId);
    }

    /**
     * Отримує форматований час створення групи
     *
     * @return форматований час
     */
    public String getFormattedCreationTime() {
        if (createdAt == null) {
            return "";
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        return createdAt.format(formatter);
    }

    /**
     * Отримує час, що минув з останнього оновлення
     *
     * @return форматований рядок з часом
     */
    public String getTimeSinceLastUpdate() {
        if (updatedAt == null) {
            return "";
        }

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(updatedAt, now);

        if (minutes < 1) {
            return "Just now";
        } else if (minutes < 60) {
            return minutes + " minute" + (minutes == 1 ? "" : "s") + " ago";
        } else {
            long hours = ChronoUnit.HOURS.between(updatedAt, now);
            if (hours < 24) {
                return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
            } else {
                long days = ChronoUnit.DAYS.between(updatedAt, now);
                return days + " day" + (days == 1 ? "" : "s") + " ago";
            }
        }
    }

    /**
     * Отримує ініціали групи для аватара
     *
     * @return ініціали групи
     */
    public String getInitials() {
        if (name == null || name.isEmpty()) {
            return "G";
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

        ChatGroup group = (ChatGroup) o;

        return id.equals(group.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}