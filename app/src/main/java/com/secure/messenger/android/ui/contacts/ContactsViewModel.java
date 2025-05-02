package com.secure.messenger.android.ui.contacts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.messenger.android.data.model.User;
import com.secure.messenger.android.data.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * ViewModel для екрану контактів
 */
public class ContactsViewModel extends AndroidViewModel {
    private static final String TAG = "ContactsViewModel";

    private final UserRepository userRepository;
    private final MutableLiveData<List<User>> contacts = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public ContactsViewModel(@NonNull Application application) {
        super(application);

        // Ініціалізація репозиторію
        userRepository = new UserRepository(application);
    }

    /**
     * Завантажує список контактів
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
                error.postValue(errorMessage);
                loading.postValue(false);
            }
        });
    }

    /**
     * Оновлює список контактів
     */
    public void refreshContacts() {
        loadContacts();
    }

    /**
     * Додає користувача до контактів
     *
     * @param userId ідентифікатор користувача
     */
    public void addContact(String userId) {
        loading.setValue(true);

        userRepository.setContact(userId, true, new UserRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                loadContacts(); // Оновлюємо список контактів
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue(errorMessage);
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
     * @return LiveData зі списком контактів
     */
    public LiveData<List<User>> getContacts() {
        return contacts;
    }

    /**
     * @return LiveData зі станом завантаження
     */
    public LiveData<Boolean> isLoading() {
        return loading;
    }

    /**
     * @return LiveData з текстом помилки
     */
    public LiveData<String> getError() {
        return error;
    }
}