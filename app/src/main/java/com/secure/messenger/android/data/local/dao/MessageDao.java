package com.secure.messenger.android.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.secure.messenger.android.data.local.entity.MessageEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO для роботи з повідомленнями в базі даних
 */
@Dao
public interface MessageDao {

    /**
     * Вставка нового повідомлення в базу даних
     *
     * @param message повідомлення
     * @return ID вставленого повідомлення
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(MessageEntity message);

    /**
     * Вставка списку повідомлень
     *
     * @param messages список повідомлень
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<MessageEntity> messages);

    /**
     * Оновлення повідомлення
     *
     * @param message повідомлення
     */
    @Update
    void update(MessageEntity message);

    /**
     * Видалення повідомлення
     *
     * @param message повідомлення
     */
    @Delete
    void delete(MessageEntity message);

    /**
     * Отримання повідомлення за ID
     *
     * @param id ідентифікатор повідомлення
     * @return повідомлення або null
     */
    @Query("SELECT * FROM messages WHERE id = :id")
    MessageEntity getMessageById(String id);

    /**
     * Отримання всіх повідомлень користувача (відправлені + отримані)
     *
     * @param userId ідентифікатор користувача
     * @return список повідомлень
     */
    @Query("SELECT * FROM messages WHERE senderId = :userId OR recipientId = :userId ORDER BY createdAt DESC")
    List<MessageEntity> getUserMessages(String userId);

    /**
     * Отримання повідомлень між двома користувачами
     *
     * @param userId1 ідентифікатор першого користувача
     * @param userId2 ідентифікатор другого користувача
     * @return список повідомлень
     */
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND recipientId = :userId2) OR (senderId = :userId2 AND recipientId = :userId1) ORDER BY createdAt ASC")
    List<MessageEntity> getMessagesBetweenUsers(String userId1, String userId2);

    /**
     * Отримання всіх повідомлень групи
     *
     * @param groupId ідентифікатор групи
     * @return список повідомлень
     */
    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY createdAt ASC")
    List<MessageEntity> getGroupMessages(String groupId);

    /**
     * Отримання всіх непрочитаних повідомлень для користувача
     *
     * @param userId ідентифікатор користувача
     * @return список непрочитаних повідомлень
     */
    @Query("SELECT * FROM messages WHERE recipientId = :userId AND isRead = 0 ORDER BY createdAt DESC")
    List<MessageEntity> getUnreadMessagesForUser(String userId);

    /**
     * Отримання повідомлень за типом
     *
     * @param messageType тип повідомлення
     * @return список повідомлень вказаного типу
     */
    @Query("SELECT * FROM messages WHERE messageType = :messageType ORDER BY createdAt DESC")
    List<MessageEntity> getMessagesByType(String messageType);

    /**
     * Отримання повідомлень, які створені після вказаного часу
     *
     * @param time час
     * @return список повідомлень
     */
    @Query("SELECT * FROM messages WHERE createdAt >= :time ORDER BY createdAt ASC")
    List<MessageEntity> getMessagesCreatedAfter(LocalDateTime time);

    /**
     * Отримання повідомлень, термін дії яких закінчується
     *
     * @param time поточний час
     * @return список повідомлень, термін дії яких закінчується
     */
    @Query("SELECT * FROM messages WHERE expiresAt <= :time")
    List<MessageEntity> getExpiredMessages(LocalDateTime time);

    /**
     * Позначення повідомлення як прочитане
     *
     * @param messageId ідентифікатор повідомлення
     */
    @Query("UPDATE messages SET isRead = 1 WHERE id = :messageId")
    void markAsRead(String messageId);

    /**
     * Позначення повідомлень як надіслані
     *
     * @param messageId ідентифікатор повідомлення
     */
    @Query("UPDATE messages SET isSent = 1 WHERE id = :messageId")
    void markAsSent(String messageId);

    /**
     * Позначення повідомлень як доставлені
     *
     * @param messageId ідентифікатор повідомлення
     */
    @Query("UPDATE messages SET isDelivered = 1 WHERE id = :messageId")
    void markAsDelivered(String messageId);

    /**
     * Видалення всіх повідомлень
     */
    @Query("DELETE FROM messages")
    void deleteAll();

    /**
     * Видалення повідомлень з певної групи
     *
     * @param groupId ідентифікатор групи
     */
    @Query("DELETE FROM messages WHERE groupId = :groupId")
    void deleteGroupMessages(String groupId);

    /**
     * Видалення повідомлень між двома користувачами
     *
     * @param userId1 ідентифікатор першого користувача
     * @param userId2 ідентифікатор другого користувача
     */
    @Query("DELETE FROM messages WHERE (senderId = :userId1 AND recipientId = :userId2) OR (senderId = :userId2 AND recipientId = :userId1)")
    void deleteMessagesBetweenUsers(String userId1, String userId2);

    /**
     * Отримання останнього повідомлення в чаті
     *
     * @param userId1 ідентифікатор першого користувача
     * @param userId2 ідентифікатор другого користувача
     * @return останнє повідомлення в чаті
     */
    @Query("SELECT * FROM messages WHERE (senderId = :userId1 AND recipientId = :userId2) OR (senderId = :userId2 AND recipientId = :userId1) ORDER BY createdAt DESC LIMIT 1")
    MessageEntity getLastMessageBetweenUsers(String userId1, String userId2);

    /**
     * Отримання останнього повідомлення в групі
     *
     * @param groupId ідентифікатор групи
     * @return останнє повідомлення в групі
     */
    @Query("SELECT * FROM messages WHERE groupId = :groupId ORDER BY createdAt DESC LIMIT 1")
    MessageEntity getLastGroupMessage(String groupId);
}