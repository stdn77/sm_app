package com.secure.messenger.android.ui.group;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.messenger.android.data.api.GroupServiceClient;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.data.model.Group;
import com.secure.messenger.android.data.model.User;
import com.secure.messenger.android.data.repository.GroupRepository;
import com.secure.messenger.android.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel для створення нової групи
 */
public class CreateGroupViewModel extends AndroidViewModel {
    private static final String TAG = "CreateGroupViewModel";

    // Репозиторії для роботи з даними
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    // LiveData для UI
    private final MutableLiveData<List<User>> contacts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> groupCreated = new MutableLiveData<>(false);
    private final MutableLiveData<Group> createdGroup = new MutableLiveData<>();

    // Вибрані контакти для групи
    private final List<String> selectedContactIds = new ArrayList<>();

    // Токен менеджер для перевірки авторизації
    private final TokenManager tokenManager;

    public CreateGroupViewModel(@NonNull Application application) {
        super(application);

        // Ініціалізація залежностей
        // В реальному додатку краще використовувати DI
        String serverHost = "10.0.2.2"; // localhost для емулятора
        int serverPort = 9090;

        // Створення клієнтів для роботи з API
        GroupServiceClient groupServiceClient = new GroupServiceClient(serverHost, serverPort);

        // Створення репозиторіїв
        this.groupRepository = new GroupRepository(application, groupServiceClient);
        this.userRepository = new UserRepository(application);

        // Ініціалізація токен менеджера
        this.tokenManager = new TokenManager(application);
    }

    /**
     * Завантажує список контактів для вибору учасників групи
     */
    public void loadContacts() {
        loading.setValue(true);
        userRepository.getAllContacts(new UserRepository.UsersCallback() {
            @Override
            public void onSuccess(List<User> userList) {
                contacts.postValue(userList);
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Помилка завантаження контактів: " + errorMessage);
                loading.postValue(false);
            }
        });
    }

    /**
     * Перемикає вибір контакту
     *
     * @param userId ідентифікатор користувача
     * @return true, якщо контакт вибрано, false - якщо вимкнено
     */
    public boolean toggleContactSelection(String userId) {
        if (selectedContactIds.contains(userId)) {
            selectedContactIds.remove(userId);
            return false;
        } else {
            selectedContactIds.add(userId);
            return true;
        }
    }

    /**
     * Перевіряє, чи вибрано контакт
     *
     * @param userId ідентифікатор користувача
     * @return true, якщо контакт вибрано
     */
    public boolean isContactSelected(String userId) {
        return selectedContactIds.contains(userId);
    }

    /**
     * Створює нову групу
     *
     * @param name назва групи
     * @param description опис групи
     * @param reportEnabled функція доповідей увімкнена
     */
    public void createGroup(String name, String description, boolean reportEnabled) {
        if (selectedContactIds.isEmpty()) {
            error.setValue("Виберіть хоча б одного учасника для групи");
            return;
        }

        loading.setValue(true);
        groupRepository.createGroup(name, description, selectedContactIds, new GroupRepository.GroupCallback() {
            @Override
            public void onSuccess(Group group) {
                createdGroup.postValue(group);

                // Якщо потрібно увімкнути звіти, робимо це окремим запитом
                if (reportEnabled) {
                    enableReportsForGroup(group.getId());
                } else {
                    groupCreated.postValue(true);
                    loading.postValue(false);
                }
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Помилка створення групи: " + errorMessage);
                loading.postValue(false);
            }
        });
    }

    /**
     * Вмикає функцію доповідей для групи
     *
     * @param groupId ідентифікатор групи
     */
    private void enableReportsForGroup(String groupId) {
        groupRepository.setReportEnabled(groupId, true, new GroupRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                groupCreated.postValue(true);
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                // Група створена, але не вдалося увімкнути звіти
                error.postValue("Групу створено, але не вдалося увімкнути звіти: " + errorMessage);
                groupCreated.postValue(true);
                loading.postValue(false);
            }
        });
    }

    /**
     * Позначає помилку як оброблену
     */
    public void errorHandled() {
        error.setValue(null);
    }

    /**
     * Скидає прапорець створення групи
     */
    public void groupCreationHandled() {
        groupCreated.setValue(false);
    }

    /**
     * Отримує кількість вибраних контактів
     *
     * @return кількість вибраних контактів
     */
    public int getSelectedContactsCount() {
        return selectedContactIds.size();
    }

    // Геттери для LiveData

    public LiveData<List<User>> getContacts() {
        return contacts;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> isGroupCreated() {
        return groupCreated;
    }

    public LiveData<Group> getCreatedGroup() {
        return createdGroup;
    }
}