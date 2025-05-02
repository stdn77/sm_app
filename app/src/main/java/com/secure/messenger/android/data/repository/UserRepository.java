package com.secure.messenger.android.data.repository;

import android.content.Context;
import android.util.Log;

import com.secure.messenger.android.data.local.AppDatabase;
import com.secure.messenger.android.data.local.dao.UserDao;
import com.secure.messenger.android.data.local.entity.UserEntity;
import com.secure.messenger.android.data.model.ModelConverter;
import com.secure.messenger.android.data.model.User;
import com.secure.messenger.proto.UserInfo;
import com.secure.messenger.proto.UserProfile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Репозиторій для роботи з користувачами
 */
public class UserRepository {
    private static final String TAG = "UserRepository";

    private final Context context;
    private final UserDao userDao;
    private final Executor executor;

    /**
     * Конструктор
     *
     * @param context контекст додатка
     */
    public UserRepository(Context context) {
        this.context = context;
        this.userDao = AppDatabase.getInstance(context).userDao();
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Отримує користувача за ідентифікатором
     *
     * @param userId ідентифікатор користувача
     * @param callback колбек з результатом
     */
    public void getUserById(String userId, UserCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity userEntity = userDao.getUserById(userId);
                if (userEntity != null) {
                    User user = ModelConverter.convertToUser(userEntity);
                    callback.onSuccess(user);
                } else {
                    callback.onError("Користувача не знайдено");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by ID: " + e.getMessage(), e);
                callback.onError("Помилка при отриманні користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує користувача за ім'ям
     *
     * @param username ім'я користувача
     * @param callback колбек з результатом
     */
    public void getUserByUsername(String username, UserCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity userEntity = userDao.getUserByUsername(username);
                if (userEntity != null) {
                    User user = ModelConverter.convertToUser(userEntity);
                    callback.onSuccess(user);
                } else {
                    callback.onError("Користувача з таким ім'ям не знайдено");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by username: " + e.getMessage(), e);
                callback.onError("Помилка при отриманні користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує користувача за номером телефону
     *
     * @param phoneNumber номер телефону
     * @param callback колбек з результатом
     */
    public void getUserByPhoneNumber(String phoneNumber, UserCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity userEntity = userDao.getUserByPhoneNumber(phoneNumber);
                if (userEntity != null) {
                    User user = ModelConverter.convertToUser(userEntity);
                    callback.onSuccess(user);
                } else {
                    callback.onError("Користувача з таким номером не знайдено");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting user by phone number: " + e.getMessage(), e);
                callback.onError("Помилка при отриманні користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує всіх користувачів
     *
     * @param callback колбек зі списком користувачів
     */
    public void getAllUsers(UsersCallback callback) {
        executor.execute(() -> {
            try {
                List<UserEntity> userEntities = userDao.getAllUsers();
                List<User> users = ModelConverter.convertToUsers(userEntities);
                callback.onSuccess(users);
            } catch (Exception e) {
                Log.e(TAG, "Error getting all users: " + e.getMessage(), e);
                callback.onError("Помилка при отриманні користувачів: " + e.getMessage());
            }
        });
    }

    /**
     * Отримує всі контакти користувача
     *
     * @param callback колбек зі списком контактів
     */
    public void getAllContacts(UsersCallback callback) {
        executor.execute(() -> {
            try {
                List<UserEntity> contactEntities = userDao.getAllContacts();
                List<User> contacts = ModelConverter.convertToUsers(contactEntities);
                callback.onSuccess(contacts);
            } catch (Exception e) {
                Log.e(TAG, "Error getting contacts: " + e.getMessage(), e);
                callback.onError("Помилка при отриманні контактів: " + e.getMessage());
            }
        });
    }

    /**
     * Шукає користувачів за ім'ям або номером телефону
     *
     * @param query пошуковий запит
     * @param callback колбек зі списком знайдених користувачів
     */
    public void searchUsers(String query, UsersCallback callback) {
        executor.execute(() -> {
            try {
                List<UserEntity> userEntities = userDao.searchUsers(query);
                List<User> users = ModelConverter.convertToUsers(userEntities);
                callback.onSuccess(users);
            } catch (Exception e) {
                Log.e(TAG, "Error searching users: " + e.getMessage(), e);
                callback.onError("Помилка при пошуку користувачів: " + e.getMessage());
            }
        });
    }

    /**
     * Додає або оновлює користувача в локальній базі даних
     *
     * @param user користувач для додавання/оновлення
     * @param callback колбек з результатом
     */
    public void addOrUpdateUser(User user, OperationCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity userEntity = new UserEntity(
                        user.getId(),
                        user.getUsername(),
                        user.getPhoneNumber(),
                        user.getStatus(),
                        user.getPublicKey(),
                        user.getLastActive(),
                        false  // За замовчуванням не є контактом
                );
                userDao.insert(userEntity);
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error adding/updating user: " + e.getMessage(), e);
                callback.onError("Помилка при додаванні/оновленні користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Додає або оновлює користувача з отриманого з сервера профілю
     *
     * @param userProfile профіль користувача з сервера
     * @param callback колбек з результатом
     */
    public void addOrUpdateUserFromProfile(UserProfile userProfile, OperationCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity userEntity = ModelConverter.convertToUserEntity(userProfile);
                userDao.insert(userEntity);
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error adding/updating user from profile: " + e.getMessage(), e);
                callback.onError("Помилка при додаванні/оновленні користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Додає або оновлює користувача з отриманої з сервера інформації
     *
     * @param userInfo інформація про користувача з сервера
     * @param callback колбек з результатом
     */
    public void addOrUpdateUserFromInfo(UserInfo userInfo, OperationCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity userEntity = ModelConverter.convertToUserEntity(userInfo);
                userDao.insert(userEntity);
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error adding/updating user from info: " + e.getMessage(), e);
                callback.onError("Помилка при додаванні/оновленні користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Оновлює інформацію про користувача
     *
     * @param user дані користувача для оновлення
     * @param callback колбек з результатом
     */
    public void updateUser(User user, OperationCallback callback) {
        executor.execute(() -> {
            try {
                // Отримуємо існуючу сутність
                UserEntity existingUser = userDao.getUserById(user.getId());
                if (existingUser == null) {
                    callback.onError("Користувача не знайдено");
                    return;
                }

                // Оновлюємо поля, зберігаючи статус контакту
                existingUser.setUsername(user.getUsername());
                existingUser.setPhoneNumber(user.getPhoneNumber());
                existingUser.setStatus(user.getStatus());
                if (user.getPublicKey() != null) {
                    existingUser.setPublicKey(user.getPublicKey());
                }
                if (user.getLastActive() != null) {
                    existingUser.setLastActive(user.getLastActive());
                }

                // Зберігаємо оновлену сутність
                userDao.update(existingUser);
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error updating user: " + e.getMessage(), e);
                callback.onError("Помилка при оновленні користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Додає або видаляє користувача з контактів
     *
     * @param userId ідентифікатор користувача
     * @param isContact чи є користувач контактом
     * @param callback колбек з результатом
     */
    public void setContact(String userId, boolean isContact, OperationCallback callback) {
        executor.execute(() -> {
            try {
                userDao.setContact(userId, isContact);
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error setting contact status: " + e.getMessage(), e);
                callback.onError("Помилка при зміні статусу контакту: " + e.getMessage());
            }
        });
    }

    /**
     * Оновлює час останньої активності користувача
     *
     * @param userId ідентифікатор користувача
     * @param lastActive час останньої активності
     * @param callback колбек з результатом
     */
    public void updateLastActive(String userId, LocalDateTime lastActive, OperationCallback callback) {
        executor.execute(() -> {
            try {
                userDao.updateLastActive(userId, lastActive);
                callback.onSuccess();
            } catch (Exception e) {
                Log.e(TAG, "Error updating last active time: " + e.getMessage(), e);
                callback.onError("Помилка при оновленні часу активності: " + e.getMessage());
            }
        });
    }

    /**
     * Видаляє користувача
     *
     * @param userId ідентифікатор користувача для видалення
     * @param callback колбек з результатом
     */
    public void deleteUser(String userId, OperationCallback callback) {
        executor.execute(() -> {
            try {
                UserEntity userEntity = userDao.getUserById(userId);
                if (userEntity != null) {
                    userDao.delete(userEntity);
                    callback.onSuccess();
                } else {
                    callback.onError("Користувача не знайдено");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error deleting user: " + e.getMessage(), e);
                callback.onError("Помилка при видаленні користувача: " + e.getMessage());
            }
        });
    }

    /**
     * Колбек для операцій з одним користувачем
     */
    public interface UserCallback {
        void onSuccess(User user);
        void onError(String error);
    }

    /**
     * Колбек для операцій з списком користувачів
     */
    public interface UsersCallback {
        void onSuccess(List<User> users);
        void onError(String error);
    }

    /**
     * Колбек для загальних операцій
     */
    public interface OperationCallback {
        void onSuccess();
        void onError(String error);
    }
}