package com.secure.messenger.android.ui.auth;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.secure.messenger.android.data.api.AuthApiService;
import com.secure.messenger.android.data.model.AuthResult;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModel для екрану реєстрації
 */
public class RegisterViewModel extends AndroidViewModel {
    private static final String TAG = "RegisterViewModel";

    private final AuthApiService authApiService;
    private final Executor executor;
    private final PhoneNumberUtil phoneNumberUtil;

    // LiveData для відслідковування стану реєстрації
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<AuthResult> registrationResultLiveData = new MutableLiveData<>();
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    /**
     * Конструктор
     *
     * @param application контекст додатка
     */
    public RegisterViewModel(@NonNull Application application) {
        super(application);
        Context context = application.getApplicationContext();

        // Ініціалізація сервісу автентифікації
        authApiService = new AuthApiService(context);

        // Ініціалізація виконавця для асинхронних операцій
        executor = Executors.newSingleThreadExecutor();

        // Ініціалізація утиліти для валідації номерів телефонів
        phoneNumberUtil = PhoneNumberUtil.getInstance();
    }

    /**
     * Реєструє нового користувача
     *
     * @param username ім'я користувача
     * @param phoneNumber номер телефону
     * @param password пароль
     * @param confirmPassword підтвердження пароля
     */
    public void register(String username, String phoneNumber, String password, String confirmPassword) {
        // Валідація введених даних
        if (!validateInput(username, phoneNumber, password, confirmPassword)) {
            return;
        }

        isLoadingLiveData.setValue(true);

        executor.execute(() -> {
            try {
                // Форматування номера телефону у міжнародний формат
                Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "UA");
                String formattedPhoneNumber = phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);

                // Реєстрація користувача через API сервіс
                AuthResult result = authApiService.register(username, formattedPhoneNumber, password, null);

                isLoadingLiveData.postValue(false);
                registrationResultLiveData.postValue(result);
            } catch (Exception e) {
                Log.e(TAG, "Registration error: " + e.getMessage(), e);
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue("Помилка реєстрації: " + e.getMessage());
            }
        });
    }

    /**
     * Валідує введені дані
     *
     * @param username ім'я користувача
     * @param phoneNumber номер телефону
     * @param password пароль
     * @param confirmPassword підтвердження пароля
     * @return true, якщо дані валідні
     */
    private boolean validateInput(String username, String phoneNumber, String password, String confirmPassword) {
        if (TextUtils.isEmpty(username)) {
            errorLiveData.setValue("Ім'я користувача не може бути порожнім");
            return false;
        }

        if (username.length() < 3) {
            errorLiveData.setValue("Ім'я користувача повинно містити мінімум 3 символи");
            return false;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            errorLiveData.setValue("Номер телефону не може бути порожнім");
            return false;
        }

        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "UA");
            if (!phoneNumberUtil.isValidNumber(number)) {
                errorLiveData.setValue("Невірний формат номера телефону");
                return false;
            }
        } catch (NumberParseException e) {
            errorLiveData.setValue("Невірний формат номера телефону");
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            errorLiveData.setValue("Пароль не може бути порожнім");
            return false;
        }

        if (password.length() < 6) {
            errorLiveData.setValue("Пароль повинен містити мінімум 6 символів");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            errorLiveData.setValue("Паролі не співпадають");
            return false;
        }

        return true;
    }

    /**
     * Обробляє помилку
     */
    public void errorHandled() {
        errorLiveData.setValue(null);
    }

    /**
     * @return LiveData зі статусом завантаження
     */
    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    /**
     * @return LiveData з результатом реєстрації
     */
    public LiveData<AuthResult> getRegistrationResult() {
        return registrationResultLiveData;
    }

    /**
     * @return LiveData з помилкою
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }
}