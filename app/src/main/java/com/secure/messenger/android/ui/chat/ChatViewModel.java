package com.secure.messenger.android.ui.chat;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.messenger.android.data.api.MessageServiceClient;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.data.local.entity.MessageEntity;
import com.secure.messenger.android.data.model.Message;
import com.secure.messenger.android.data.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModel для управління чатом
 */
public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";

    private final MessageRepository messageRepository;
    private final TokenManager tokenManager;
    private final Executor executor;

    private String chatId;
    private boolean isGroup;

    // LiveData для відстеження стану
    private final MutableLiveData<List<Message>> messagesLiveData = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> sendMessageStatus = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public ChatViewModel(@NonNull Application application) {
        super(application);

        // Ініціалізація залежностей
        MessageServiceClient messageServiceClient = new MessageServiceClient("localhost", 9090); // Змініть на реальні дані
        this.messageRepository = new MessageRepository(application, messageServiceClient);
        this.tokenManager = new TokenManager(application);
        this.executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Ініціалізує ViewModel з даними чату
     *
     * @param chatId  ідентифікатор чату
     * @param isGroup чи є чат груповим
     */
    public void init(String chatId, boolean isGroup) {
        this.chatId = chatId;
        this.isGroup = isGroup;
    }

    /**
     * Завантажує повідомлення чату
     */
    public void loadMessages() {
        executor.execute(() -> {
            try {
                if (isGroup) {
                    // Завантаження групових повідомлень
                    messageRepository.getGroupMessages(chatId, new MessageRepository.FetchMessagesCallback() {
                        @Override
                        public void onSuccess(List<MessageEntity> messages) {
                            // Конвертуємо сутності в моделі для UI
                            List<Message> messageList = convertMessageEntities(messages);
                            messagesLiveData.postValue(messageList);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            errorLiveData.postValue("Помилка завантаження повідомлень: " + errorMessage);
                        }
                    });
                } else {
                    // Завантаження приватних повідомлень
                    messageRepository.getDirectMessages(chatId, new MessageRepository.FetchMessagesCallback() {
                        @Override
                        public void onSuccess(List<MessageEntity> messages) {
                            // Конвертуємо сутності в моделі для UI
                            List<Message> messageList = convertMessageEntities(messages);
                            messagesLiveData.postValue(messageList);
                        }

                        @Override
                        public void onError(String errorMessage) {
                            errorLiveData.postValue("Помилка завантаження повідомлень: " + errorMessage);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading messages: " + e.getMessage(), e);
                errorLiveData.postValue("Помилка: " + e.getMessage());
            }
        });
    }

    /**
     * Надсилає текстове повідомлення
     *
     * @param text текст повідомлення
     */
    public void sendTextMessage(String text) {
        executor.execute(() -> {
            try {
                if (isGroup) {
                    // Надсилання групового повідомлення
                    messageRepository.sendGroupMessage(chatId, text, "TEXT", new MessageRepository.MessageCallback() {
                        @Override
                        public void onSuccess(String messageId) {
                            sendMessageStatus.postValue(true);
                            loadMessages(); // Оновлюємо список повідомлень
                        }

                        @Override
                        public void onError(String errorMessage) {
                            sendMessageStatus.postValue(false);
                            errorLiveData.postValue("Помилка відправки повідомлення: " + errorMessage);
                        }
                    });
                } else {
                    // Надсилання приватного повідомлення
                    messageRepository.sendDirectMessage(chatId, text, "TEXT", new MessageRepository.MessageCallback() {
                        @Override
                        public void onSuccess(String messageId) {
                            sendMessageStatus.postValue(true);
                            loadMessages(); // Оновлюємо список повідомлень
                        }

                        @Override
                        public void onError(String errorMessage) {
                            sendMessageStatus.postValue(false);
                            errorLiveData.postValue("Помилка відправки повідомлення: " + errorMessage);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error sending message: " + e.getMessage(), e);
                sendMessageStatus.postValue(false);
                errorLiveData.postValue("Помилка: " + e.getMessage());
            }
        });
    }

    /**
     * Позначає повідомлення як прочитане
     *
     * @param messageId ідентифікатор повідомлення
     */
    public void markMessageAsRead(String messageId) {
        executor.execute(() -> {
            try {
                messageRepository.markMessageAsRead(messageId, new MessageRepository.MessageCallback() {
                    @Override
                    public void onSuccess(String messageId) {
                        loadMessages(); // Оновлюємо список повідомлень
                    }

                    @Override
                    public void onError(String errorMessage) {
                        errorLiveData.postValue("Помилка позначення повідомлення як прочитане: " + errorMessage);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error marking message as read: " + e.getMessage(), e);
                errorLiveData.postValue("Помилка: " + e.getMessage());
            }
        });
    }

    /**
     * Видаляє повідомлення
     *
     * @param messageId ідентифікатор повідомлення
     */
    public void deleteMessage(String messageId) {
        executor.execute(() -> {
            try {
                messageRepository.deleteMessage(messageId, new MessageRepository.MessageCallback() {
                    @Override
                    public void onSuccess(String messageId) {
                        loadMessages(); // Оновлюємо список повідомлень
                    }

                    @Override
                    public void onError(String errorMessage) {
                        errorLiveData.postValue("Помилка видалення повідомлення: " + errorMessage);
                    }
                });
            } catch (Exception e) {
                Log.e(TAG, "Error deleting message: " + e.getMessage(), e);
                errorLiveData.postValue("Помилка: " + e.getMessage());
            }
        });
    }

    /**
     * Конвертує сутності повідомлень у моделі для UI
     *
     * @param entities список сутностей повідомлень
     * @return список моделей повідомлень
     */
    private List<Message> convertMessageEntities(List<MessageEntity> entities) {
        List<Message> messages = new ArrayList<>();
        for (MessageEntity entity : entities) {
            Message message = new Message(
                    entity.getId(),
                    entity.getSenderId(),
                    entity.getRecipientId(),
                    entity.getGroupId(),
                    entity.getMessageType(),
                    entity.getEncryptedContent(),
                    entity.getCreatedAt(),
                    entity.getExpiresAt(),
                    entity.isRead(),
                    entity.isSent(),
                    entity.isDelivered()
            );
            messages.add(message);
        }
        return messages;
    }

    /**
     * Отримує LiveData зі списком повідомлень
     *
     * @return LiveData зі списком повідомлень
     */
    public LiveData<List<Message>> getMessages() {
        return messagesLiveData;
    }

    /**
     * Отримує LiveData зі статусом відправки повідомлення
     *
     * @return LiveData зі статусом відправки повідомлення
     */
    public LiveData<Boolean> getSendMessageStatus() {
        return sendMessageStatus;
    }

    /**
     * Скидає статус відправки повідомлення
     */
    public void resetSendMessageStatus() {
        sendMessageStatus.setValue(null);
    }

    /**
     * Отримує LiveData з помилками
     *
     * @return LiveData з помилками
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * Скидає помилку
     */
    public void resetError() {
        errorLiveData.setValue(null);
    }
}