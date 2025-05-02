package com.secure.messenger.android.ui.group;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.messenger.android.data.api.MessageServiceClient;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.data.local.entity.MessageEntity;
import com.secure.messenger.android.data.model.Group;
import com.secure.messenger.android.data.model.Message;
import com.secure.messenger.android.data.repository.GroupRepository;
import com.secure.messenger.android.data.repository.MessageRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel для групового чату
 */
public class GroupChatViewModel extends AndroidViewModel {
    private static final String TAG = "GroupChatViewModel";

    // Репозиторії для роботи з даними
    private final GroupRepository groupRepository;
    private final MessageRepository messageRepository;

    // LiveData для UI
    private final MutableLiveData<Group> group = new MutableLiveData<>();
    private final MutableLiveData<List<Message>> messages = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> sendingMessage = new MutableLiveData<>(false);

    // Ідентифікатор групи
    private String groupId;

    // Токен менеджер для перевірки авторизації
    private final TokenManager tokenManager;

    public GroupChatViewModel(@NonNull Application application) {
        super(application);

        // Ініціалізація залежностей
        // В реальному додатку краще використовувати DI
        String serverHost = "10.0.2.2"; // localhost для емулятора
        int serverPort = 9090;

        // Створення клієнтів для роботи з API
        MessageServiceClient messageServiceClient = new MessageServiceClient(serverHost, serverPort);

        // Створення репозиторіїв
        this.groupRepository = new GroupRepository(application, null);
        this.messageRepository = new MessageRepository(application, messageServiceClient);

        // Ініціалізація токен менеджера
        this.tokenManager = new TokenManager(application);
    }

    /**
     * Встановлює ідентифікатор групи
     *
     * @param groupId ідентифікатор групи
     */
    public void setGroupId(String groupId) {
        this.groupId = groupId;
        loadGroup();
    }

    /**
     * Завантажує інформацію про групу
     */
    private void loadGroup() {
        if (groupId == null) {
            error.setValue("Ідентифікатор групи не вказаний");
            return;
        }

        loading.setValue(true);
        groupRepository.getGroupById(groupId, new GroupRepository.GroupCallback() {
            @Override
            public void onSuccess(Group result) {
                group.postValue(result);
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Помилка завантаження групи: " + errorMessage);
                loading.postValue(false);
            }
        });
    }

    /**
     * Завантажує повідомлення групи
     */
    public void loadMessages() {
        if (groupId == null) {
            error.setValue("Ідентифікатор групи не вказаний");
            return;
        }

        loading.setValue(true);
        messageRepository.getGroupMessages(groupId, new MessageRepository.FetchMessagesCallback() {
            @Override
            public void onSuccess(List<MessageEntity> messageEntities) {
                // Конвертуємо сутності в моделі
                List<Message> messageList = new ArrayList<>();
                for (MessageEntity entity : messageEntities) {
                    try {
                        // TODO: Реалізувати конвертацію MessageEntity в Message
                        // messageList.add(convertEntityToMessage(entity));
                    } catch (Exception e) {
                        Log.e(TAG, "Error converting message entity: " + e.getMessage(), e);
                    }
                }
                messages.postValue(messageList);
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Помилка завантаження повідомлень: " + errorMessage);
                loading.postValue(false);
            }
        });
    }

    /**
     * Надсилає повідомлення в групу
     *
     * @param text текст повідомлення
     */
    public void sendMessage(String text) {
        if (groupId == null) {
            error.setValue("Ідентифікатор групи не вказаний");
            return;
        }

        sendingMessage.setValue(true);
        messageRepository.sendGroupMessage(groupId, text, "TEXT", new MessageRepository.MessageCallback() {
            @Override
            public void onSuccess(String messageId) {
                // Оновлюємо список повідомлень
                loadMessages();
                sendingMessage.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Помилка відправки повідомлення: " + errorMessage);
                sendingMessage.postValue(false);
            }
        });
    }

    /**
     * Позначає повідомлення як прочитане
     *
     * @param messageId ідентифікатор повідомлення
     */
    public void markMessageAsRead(String messageId) {
        messageRepository.markMessageAsRead(messageId, new MessageRepository.MessageCallback() {
            @Override
            public void onSuccess(String msgId) {
                // Повідомлення успішно позначено як прочитане
            }

            @Override
            public void onError(String errorMessage) {
                Log.e(TAG, "Error marking message as read: " + errorMessage);
            }
        });
    }

    /**
     * Позначає помилку як оброблену
     */
    public void errorHandled() {
        error.setValue(null);
    }

    // Геттери для LiveData

    public LiveData<Group> getGroup() {
        return group;
    }

    public LiveData<List<Message>> getMessages() {
        return messages;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> isSendingMessage() {
        return sendingMessage;
    }
}