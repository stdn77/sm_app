package com.secure.messenger.android.data.local.dao;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import com.secure.messenger.android.data.local.entity.UserEntity;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DAO для роботи з користувачами в базі даних
 */
@Dao
public interface UserDao {

    /**
     * Вставка нового користувача в базу даних
     *
     * @param user користувач
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(UserEntity user);

    /**
     * Вставка списку користувачів
     *
     * @param users список користувачів
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<UserEntity> users);

    /**
     * Оновлення користувача
     *
     * @param user користувач
     */
    @Update
    void update(UserEntity user);

    /**
     * Видалення користувача
     *
     * @param user користувач
     */
    @Delete
    void delete(UserEntity user);

    /**
     * Отримання користувача за ID
     *
     * @param id ідентифікатор користувача
     * @return користувач або null
     */
    @Query("SELECT * FROM users WHERE id = :id")
    UserEntity getUserById(String id);

    /**
     * Отримання користувача за ім'ям
     *
     * @param username ім'я користувача
     * @return користувач або null
     */
    @Query("SELECT * FROM users WHERE username = :username")
    UserEntity getUserByUsername(String username);

    /**
     * Отримання користувача за номером телефону
     *
     * @param phoneNumber номер телефону
     * @return користувач або null
     */
    @Query("SELECT * FROM users WHERE phoneNumber = :phoneNumber")
    UserEntity getUserByPhoneNumber(String phoneNumber);

    /**
     * Отримання всіх користувачів
     *
     * @return список користувачів
     */
    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    /**
     * Отримання всіх контактів
     *
     * @return список контактів
     */
    @Query("SELECT * FROM users WHERE isContact = 1")
    List<UserEntity> getAllContacts();

    /**
     * Отримання користувачів, які були активні після вказаного часу
     *
     * @param time час
     * @return список активних користувачів
     */
    @Query("SELECT * FROM users WHERE lastActive >= :time")
    List<UserEntity> getActiveUsersSince(LocalDateTime time);

    /**
     * Пошук користувачів за ім'ям або номером телефону
     *
     * @param query пошуковий запит
     * @return список знайдених користувачів
     */
    @Query("SELECT * FROM users WHERE username LIKE '%' || :query || '%' OR phoneNumber LIKE '%' || :query || '%'")
    List<UserEntity> searchUsers(String query);

    /**
     * Видалення всіх користувачів
     */
    @Query("DELETE FROM users")
    void deleteAll();

    /**
     * Встановлення користувача як контакт
     *
     * @param userId ідентифікатор користувача
     * @param isContact чи є користувач контактом
     */
    @Query("UPDATE users SET isContact = :isContact WHERE id = :userId")
    void setContact(String userId, boolean isContact);

    /**
     * Оновлення часу останньої активності користувача
     *
     * @param userId ідентифікатор користувача
     * @param lastActive час останньої активності
     */
    @Query("UPDATE users SET lastActive = :lastActive WHERE id = :userId")
    void updateLastActive(String userId, LocalDateTime lastActive);
}