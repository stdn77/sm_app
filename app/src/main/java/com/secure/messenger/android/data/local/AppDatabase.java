package com.secure.messenger.android.data.local;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.secure.messenger.android.data.local.converter.DateConverter;
import com.secure.messenger.android.data.local.dao.ChatGroupDao;
import com.secure.messenger.android.data.local.dao.MessageDao;
import com.secure.messenger.android.data.local.dao.UserDao;
import com.secure.messenger.android.data.local.entity.ChatGroupEntity;
import com.secure.messenger.android.data.local.entity.MessageEntity;
import com.secure.messenger.android.data.local.entity.UserEntity;

/**
 * Головний клас бази даних Room для локального зберігання даних
 */
@Database(
        entities = {
                UserEntity.class,
                MessageEntity.class,
                ChatGroupEntity.class
        },
        version = 1,
        exportSchema = false
)
@TypeConverters({DateConverter.class})
public abstract class AppDatabase extends RoomDatabase {

    private static final String DATABASE_NAME = "secure_messenger.db";
    private static AppDatabase instance;

    /**
     * Отримує DAO для роботи з користувачами
     * @return UserDao
     */
    public abstract UserDao userDao();

    /**
     * Отримує DAO для роботи з повідомленнями
     * @return MessageDao
     */
    public abstract MessageDao messageDao();

    /**
     * Отримує DAO для роботи з групами
     * @return ChatGroupDao
     */
    public abstract ChatGroupDao chatGroupDao();

    /**
     * Отримує або створює екземпляр бази даних
     *
     * @param context контекст додатка
     * @return екземпляр AppDatabase
     */
    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}