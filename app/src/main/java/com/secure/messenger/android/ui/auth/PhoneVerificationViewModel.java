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

import java.util.Random;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModel для екрану верифікації номера телефону
 */
public class PhoneVerificationViewModel extends AndroidViewModel {
    private static final String TAG = "PhoneVerificationVM";

    private final PhoneNumberUtil phoneNumberUtil;
    private final Executor executor;
    private final Context context;

    // LiveData для відслідковування стану верифікації
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> codeSentLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> codeVerifiedLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    // Тимчасове зберігання даних верифікації
    private String verificationCode;
    private String phoneNumber;

    /**
     * Конструктор
     *
     * @param application контекст додатка
     */
    public PhoneVerificationViewModel(@NonNull Application application) {
        super(application);
        this.context = application.getApplicationContext();

        // Ініціалізація утиліти для валідації номерів телефонів
        phoneNumberUtil = PhoneNumberUtil.getInstance();

        // Ініціалізація виконавця для асинхронних операцій
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Надсилає код верифікації на вказаний номер телефону
     *
     * @param phoneNumber номер телефону
     */
    public void sendVerificationCode(String phoneNumber) {
        // Валідація номера телефону
        if (!validatePhoneNumber(phoneNumber)) {
            return;
        }

        this.phoneNumber = phoneNumber;
        isLoadingLiveData.setValue(true);

        executor.execute(() -> {
            try {
                // В реальному додатку тут був би запит до сервера для надсилання SMS
                // Для демонстрації генеруємо випадковий код
                verificationCode = generateVerificationCode();
                Log.d(TAG, "Generated verification code: " + verificationCode); // Для тестування

                // Імітація затримки мережі
                Thread.sleep(1000);

                isLoadingLiveData.postValue(false);
                codeSentLiveData.postValue(true);
            } catch (Exception e) {
                Log.e(TAG, "Error sending verification code: " + e.getMessage(), e);
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue("Помилка надсилання коду: " + e.getMessage());
            }
        });
    }

    /**
     * Перевіряє введений код верифікації
     *
     * @param code введений код
     */
    public void verifyCode(String code) {
        if (TextUtils.isEmpty(code)) {
            errorLiveData.setValue("Введіть код верифікації");
            return;
        }

        isLoadingLiveData.setValue(true);

        executor.execute(() -> {
            try {
                // Для демонстрації просто порівнюємо з згенерованим кодом
                boolean isCodeValid = code.equals(verificationCode);

                // Імітація затримки мережі
                Thread.sleep(1000);

                isLoadingLiveData.postValue(false);
                if (isCodeValid) {
                    codeVerifiedLiveData.postValue(true);
                } else {
                    errorLiveData.postValue("Невірний код верифікації");
                }
            } catch (Exception e) {
                Log.e(TAG, "Error verifying code: " + e.getMessage(), e);
                isLoadingLiveData.postValue(false);
                errorLiveData.postValue("Помилка перевірки коду: " + e.getMessage());
            }
        });
    }

    /**
     * Валідує формат номера телефону
     *
     * @param phoneNumber номер телефону
     * @return true, якщо номер валідний
     */
    private boolean validatePhoneNumber(String phoneNumber) {
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
            return true;
        } catch (NumberParseException e) {
            errorLiveData.setValue("Невірний формат номера телефону");
            return false;
        }
    }

    /**
     * Генерує випадковий 6-значний код верифікації
     *
     * @return згенерований код
     */
    private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // Випадкове 6-значне число
        return String.valueOf(code);
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
     * @return LiveData зі статусом надсилання коду
     */
    public LiveData<Boolean> getCodeSentLiveData() {
        return codeSentLiveData;
    }

    /**
     * @return LiveData зі статусом верифікації коду
     */
    public LiveData<Boolean> getCodeVerifiedLiveData() {
        return codeVerifiedLiveData;
    }

    /**
     * @return LiveData з помилкою
     */
    public LiveData<String> getError() {
        return errorLiveData;
    }

    /**
     * @return Номер телефону
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }
}