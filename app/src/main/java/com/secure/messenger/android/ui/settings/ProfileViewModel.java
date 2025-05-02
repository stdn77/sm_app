package com.secure.messenger.android.ui.settings;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.data.model.User;
import com.secure.messenger.android.data.repository.UserRepository;

/**
 * ViewModel для профілю користувача
 */
public class ProfileViewModel extends AndroidViewModel {
    private static final String TAG = "ProfileViewModel";

    private final UserRepository userRepository;
    private final TokenManager tokenManager;

    private final MutableLiveData<User> userProfile = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> profileUpdated = new MutableLiveData<>(false);

    public ProfileViewModel(@NonNull Application application) {
        super(application);

        // Ініціалізація залежностей
        userRepository = new UserRepository(application);
        tokenManager = new TokenManager(application);
    }

    /**
     * Завантажує профіль користувача
     */
    public void loadUserProfile() {
        loading.setValue(true);

        // Отримання ідентифікатора поточного користувача
        String userId = tokenManager.getUserId();
        if (userId == null) {
            error.postValue("Неможливо отримати ідентифікатор користувача");
            loading.postValue(false);
            return;
        }

        userRepository.getUserById(userId, new UserRepository.UserCallback() {
            @Override
            public void onSuccess(User user) {
                userProfile.postValue(user);
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Помилка завантаження профілю: " + errorMessage);
                loading.postValue(false);
            }
        });
    }

    /**
     * Оновлює профіль користувача
     *
     * @param username нове ім'я користувача
     * @param status новий статус
     */
    public void updateUserProfile(String username, String status) {
        loading.setValue(true);

        // Отримання поточного користувача
        User currentUser = userProfile.getValue();
        if (currentUser == null) {
            error.postValue("Неможливо оновити профіль: дані користувача не завантажені");
            loading.postValue(false);
            return;
        }

        // Оновлення даних
        currentUser.setUsername(username);
        currentUser.setStatus(status);

        userRepository.updateUser(currentUser, new UserRepository.OperationCallback() {
            @Override
            public void onSuccess() {
                userProfile.postValue(currentUser);
                profileUpdated.postValue(true);
                loading.postValue(false);
            }

            @Override
            public void onError(String errorMessage) {
                error.postValue("Помилка оновлення профілю: " + errorMessage);
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
     * Позначає успішне оновлення профілю як оброблене
     */
    public void profileUpdateHandled() {
        profileUpdated.setValue(false);
    }

    // Геттери для LiveData

    public LiveData<User> getUserProfile() {
        return userProfile;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> isProfileUpdated() {
        return profileUpdated;
    }
}