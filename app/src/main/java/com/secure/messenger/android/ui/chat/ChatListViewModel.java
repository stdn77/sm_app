package com.secure.messenger.android.ui.chat;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.messenger.android.data.api.MessageServiceClient;
import com.secure.messenger.android.data.local.AppDatabase;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.data.local.dao.MessageDao;
import com.secure.messenger.android.data.local.dao.UserDao;
import com.secure.messenger.android.data.local.entity.MessageEntity;
import com.secure.messenger.android.data.local.entity.UserEntity;
import com.secure.messenger.android.data.model.ChatPreview;
import com.secure.messenger.android.data.model.Message;
import com.secure.messenger.android.data.repository.MessageRepository;
import com.secure.messenger.android.data.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ViewModel для списку чатів
 */
public class ChatListViewModel extends AndroidViewModel {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final MessageDao messageDao;
    private final UserDao userDao;
    private final TokenManager tokenManager;

    private final MutableLiveData<List<ChatPreview>> chatsLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    private String currentUserId;
    private long lastSyncTimestamp;

    /**
     * Конструктор
     *
     * @param application контекст додатка
     */
    public ChatListViewModel(@NonNull Application application) {
        super(application);

        // Ініціалізація компонентів
        Context context = application.getApplicationContext();
        AppDatabase db = AppDatabase.getInstance(context);
        messageDao = db.messageDao();
        userDao = db.userDao();
        tokenManager = new TokenManager(context);

        // Створення клієнта для сервісу повідомлень
        String serverHost = "10.0.2.2"; // localhost для емулятора
        int serverPort = 9090;
        MessageServiceClient messageServiceClient = new MessageServiceClient(serverHost, serverPort);

        // Ініціалізація репозиторіїв
        messageRepository = new MessageRepository(context, messageServiceClient);
        userRepository = new UserRepository(context);

        // Отримання ідентифікатора поточного користувача
        currentUserId = tokenManager.getUserId();

        // Ініціалізація часу останньої синхронізації
        lastSyncTimestamp = System.currentTimeMillis() - (24 * 60 * 60 * 1000); // 24 години тому
    }

    /**
     * Завантажує список чатів
     */
    public void loadChats() {
        isLoadingLiveData.setValue(true);

        // Отримання чатів з локальної бази даних
        loadChatsFromLocalDb();

        // Синхронізація з сервером
        syncWithServer();
    }

    /**
     * Оновлює список чатів (примусова синхронізація з сервером)
     */
    public void refreshChats() {
        isLoadingLiveData.setValue(true);
        syncWithServer();
    }

    /**
     * Завантажує чати з локальної бази даних
     */
    private void loadChatsFromLocalDb() {
        try {
            // Отримання всіх активних чатів
            Map<String, ChatPreview> chatMap = new HashMap<>();

            // Отримання приватних чатів
            List<UserEntity> contacts = userDao.getAllContacts();
            for (UserEntity contact : contacts) {
                // Отримання останнього повідомлення
                MessageEntity lastMessage = messageDao.getLastMessageBetweenUsers(currentUserId, contact.getId());

                if (lastMessage != null) {
                    // Створення превью чату
                    ChatPreview chatPreview = createChatPreview(contact, lastMessage, false);
                    chatMap.put(contact.getId(), chatPreview);
                }
            }

            // TODO: Додати завантаження групових чатів

            // Оновлення LiveData
            chatsLiveData.postValue(new ArrayList<>(chatMap.values()));
        } catch (Exception e) {
            errorLiveData.postValue("Помилка при завантаженні чатів: " + e.getMessage());
        }
    }

    /**
     * Синхронізує повідомлення з сервером
     */
    private void syncWithServer() {
        messageRepository.fetchMessages(lastSyncTimestamp, new MessageRepository.FetchMessagesCallback() {
            @Override
            public void onSuccess(List<MessageEntity> messages) {
                // Оновлення часу останньої синхронізації
                lastSyncTimestamp = System.currentTimeMillis();

                // Оновлення списку чатів
                loadChatsFromLocalDb();

                isLoadingLiveData.postValue(false);
            }

            @Override
            public void onError(String error) {
                errorLiveData.postValue("Помилка синхронізації з сервером: " + error);
                isLoadingLiveData.postValue(false);
            }
        });
    }

    /**
     * Створює превью чату на основі інформації про користувача та останнього повідомлення
     *
     * @param user користувач
     * @param lastMessage останнє повідомлення
     * @param isGroup чи є це групою
     * @return превью чату
     */
    private ChatPreview createChatPreview(UserEntity user, MessageEntity lastMessage, boolean isGroup) {
        // Отримання тексту останнього повідомлення
        String messageText = getMessagePreview(lastMessage);

        // Отримання кількості непрочитаних повідомлень
        int unreadCount = getUnreadMessageCount(user.getId());

        return new ChatPreview(
                user.getId(),
                user.getUsername(),
                messageText,
                lastMessage.getCreatedAt(),
                unreadCount,
                isGroup,
                user.getLastActive() != null && user.getLastActive().isAfter(LocalDateTime.now().minusMinutes(5)),
                null // URL аватара
        );
    }

    /**
     * Отримує текстовий вміст повідомлення для відображення у превью
     *
     * @param messageEntity сутність повідомлення
     * @return текстовий вміст повідомлення
     */
    private String getMessagePreview(MessageEntity messageEntity) {
        // TODO: Замінити на розшифровку повідомлення
        if ("TEXT".equals(messageEntity.getMessageType())) {
            return "Текстове повідомлення";
        } else if ("IMAGE".equals(messageEntity.getMessageType())) {
            return "Зображення";
        } else if ("DOCUMENT".equals(messageEntity.getMessageType())) {
            return "Документ";
        } else if ("VOICE".equals(messageEntity.getMessageType())) {
            return "Голосове повідомлення";
        } else {
            return "Повідомлення";
        }
    }

    /**
     * Отримує кількість непрочитаних повідомлень від конкретного користувача
     *
     * @param userId ідентифікатор користувача
     * @return кількість непрочитаних повідомлень
     */
    private int getUnreadMessageCount(String userId) {
        try {
            List<MessageEntity> unreadMessages = messageDao.getUnreadMessagesForUser(currentUserId);

            int count = 0;
            for (MessageEntity message : unreadMessages) {
                if (message.getSenderId().equals(userId)) {
                    count++;
                }
            }

            return count;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Обробляє помилку
     */
    public void errorHandled() {
        errorLiveData.setValue(null);
    }

    /**
     * @return LiveData зі списком чатів
     */
    public LiveData<List<ChatPreview>> getChats() {
        return chatsLiveData;
    }

    /**
     * @return LiveData зі статусом завантаження
     */
    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    /**
     * @return LiveData з помилкою
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }
}