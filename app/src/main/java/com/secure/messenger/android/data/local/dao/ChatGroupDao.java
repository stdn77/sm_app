package com.secure.messenger.android.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.secure.messenger.android.data.local.entity.ChatGroupEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO для роботи з групами чату в базі даних
 */
@Dao
public interface ChatGroupDao {

    /**
     * Вставка нової групи в базу даних
     *
     * @param group група
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatGroupEntity group);

    /**
     * Вставка списку груп
     *
     * @param groups список груп
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<ChatGroupEntity> groups);

    /**
     * Оновлення групи
     *
     * @param group група
     */
    @Update
    void update(ChatGroupEntity group);

    /**
     * Видалення групи
     *
     * @param group група
     */
    @Delete
    void delete(ChatGroupEntity group);

    /**
     * Отримання групи за ID
     *
     * @param id ідентифікатор групи
     * @return група або null
     */
    @Query("SELECT * FROM chat_groups WHERE id = :id")
    ChatGroupEntity getGroupById(String id);

    /**
     * Отримання груп, де користувач є адміністратором
     *
     * @param userId ідентифікатор користувача
     * @return список груп
     */
    @Query("SELECT * FROM chat_groups WHERE adminId = :userId")
    List<ChatGroupEntity> getGroupsByAdmin(String userId);

    /**
     * Отримання всіх груп
     *
     * @return список груп
     */
    @Query("SELECT * FROM chat_groups")
    List<ChatGroupEntity> getAllGroups();

    /**
     * Пошук груп за назвою
     *
     * @param query пошуковий запит
     * @return список знайдених груп
     */
    @Query("SELECT * FROM chat_groups WHERE name LIKE '%' || :query || '%'")
    List<ChatGroupEntity> searchGroups(String query);

    /**
     * Отримання груп з увімкненими звітами
     *
     * @return список груп з увімкненими звітами
     */
    @Query("SELECT * FROM chat_groups WHERE reportEnabled = 1")
    List<ChatGroupEntity> getGroupsWithEnabledReporting();

    /**
     * Оновлення кількості учасників групи
     *
     * @param groupId ідентифікатор групи
     * @param memberCount кількість учасників
     */
    @Query("UPDATE chat_groups SET memberCount = :memberCount WHERE id = :groupId")
    void updateMemberCount(String groupId, int memberCount);

    /**
     * Увімкнення/вимкнення звітів для групи
     *
     * @param groupId ідентифікатор групи
     * @param enabled статус увімкнення
     */
    @Query("UPDATE chat_groups SET reportEnabled = :enabled WHERE id = :groupId")
    void setReportEnabled(String groupId, boolean enabled);

    /**
     * Отримання груп, оновлених після вказаного часу
     *
     * @param time час
     * @return список оновлених груп
     */
    @Query("SELECT * FROM chat_groups WHERE updatedAt >= :time")
    List<ChatGroupEntity> getGroupsUpdatedAfter(LocalDateTime time);

    /**
     * Видалення всіх груп
     */
    @Query("DELETE FROM chat_groups")
    void deleteAll();

    /**
     * Перевірка існування групи з вказаним ID
     *
     * @param groupId ідентифікатор групи
     * @return true, якщо група існує
     */
    @Query("SELECT COUNT(*) > 0 FROM chat_groups WHERE id = :groupId")
    boolean exists(String groupId);

    /**
     * Оновлення часу останнього оновлення групи
     *
     * @param groupId ідентифікатор групи
     * @param updatedAt час оновлення
     */
    @Query("UPDATE chat_groups SET updatedAt = :updatedAt WHERE id = :groupId")
    void updateLastUpdateTime(String groupId, LocalDateTime updatedAt);

    /**
     * Оновлення зашифрованого ключа групи
     *
     * @param groupId ідентифікатор групи
     * @param encryptedKey зашифрований ключ
     */
    @Query("UPDATE chat_groups SET encryptedGroupKey = :encryptedKey WHERE id = :groupId")
    void updateEncryptedGroupKey(String groupId, byte[] encryptedKey);
}