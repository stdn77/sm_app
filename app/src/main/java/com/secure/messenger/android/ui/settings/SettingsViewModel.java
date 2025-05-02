package com.secure.messenger.android.ui.settings;

import android.app.Application;
import android.util.Pair;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.messenger.android.util.StorageUtils;

import java.io.File;

/**
 * ViewModel для налаштувань додатку
 */
public class SettingsViewModel extends AndroidViewModel {
    private static final String TAG = "SettingsViewModel";

    private final MutableLiveData<Pair<String, String>> storageInfo = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cacheCleared = new MutableLiveData<>(false);

    public SettingsViewModel(@NonNull Application application) {
        super(application);
    }

    /**
     * Завантажує інформацію про використання сховища
     */
    public void loadStorageInfo() {
        loading.setValue(true);

        try {
            // Отримання розміру даних програми
            File appDataDir = getApplication().getFilesDir();
            long appDataSize = StorageUtils.getDirSize(appDataDir);
            String formattedAppDataSize = StorageUtils.formatSize(appDataSize);

            // Отримання розміру кешу
            File cacheDir = getApplication().getCacheDir();
            long cacheSize = StorageUtils.getDirSize(cacheDir);
            String formattedCacheSize = StorageUtils.formatSize(cacheSize);

            storageInfo.postValue(new Pair<>(formattedAppDataSize, formattedCacheSize));
        } catch (Exception e) {
            error.postValue("Помилка отримання інформації про сховище: " + e.getMessage());
        } finally {
            loading.postValue(false);
        }
    }

    /**
     * Очищає кеш додатку
     */
    public void clearCache() {
        loading.setValue(true);

        try {
            File cacheDir = getApplication().getCacheDir();
            boolean success = StorageUtils.deleteDir(cacheDir);

            if (success) {
                cacheCleared.postValue(true);
            } else {
                error.postValue("Не вдалося очистити кеш");
            }
        } catch (Exception e) {
            error.postValue("Помилка очищення кешу: " + e.getMessage());
        } finally {
            loading.postValue(false);
        }
    }

    /**
     * Оновлює налаштування сповіщень
     *
     * @param enabled статус сповіщень
     */
    public void updateNotificationSettings(boolean enabled) {
        // Тут можна додати логіку для підписки/відписки на сповіщення від сервера
    }

    /**
     * Оновлює налаштування теми
     *
     * @param isDarkTheme чи увімкнена темна тема
     */
    public void updateThemeSettings(boolean isDarkTheme) {
        // Тут можна додати логіку для застосування теми
    }

    /**
     * Позначає помилку як оброблену
     */
    public void errorHandled() {
        error.setValue(null);
    }

    /**
     * Позначає успішне очищення кешу як оброблене
     */
    public void cacheClearHandled() {
        cacheCleared.setValue(false);
    }

    // Геттери для LiveData

    public LiveData<Pair<String, String>> getStorageInfo() {
        return storageInfo;
    }

    public LiveData<Boolean> isLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> isCacheCleared() {
        return cacheCleared;
    }
}