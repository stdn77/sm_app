package com.secure.messenger.android.ui.auth;

import android.app.Application;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

// Припустимо, що є сервіс для виклику API верифікації
// import com.secure.messenger.android.data.api.VerificationApiService;
// import com.secure.messenger.android.data.model.VerificationResult;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * ViewModel для екрану введення коду верифікації
 */
public class CodeVerificationViewModel extends AndroidViewModel {
    private static final String TAG = "CodeVerificationVM";

    // TODO: Замініть на реальний API сервіс або репозиторій
    // private final VerificationApiService verificationApiService;
    private final Executor executor;

    // LiveData для відслідковування стану
    private final MutableLiveData<Boolean> isLoadingLiveData = new MutableLiveData<>(false);
    private final MutableLiveData<Boolean> verificationResultLiveData = new MutableLiveData<>(); // true = успіх, false = невдача
    private final MutableLiveData<String> errorLiveData = new MutableLiveData<>();

    public CodeVerificationViewModel(@NonNull Application application) {
        super(application);
        // TODO: Ініціалізуйте ваш реальний API сервіс (використовуйте DI)
        // verificationApiService = new VerificationApiService(application.getApplicationContext());
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Перевіряє введений код верифікації
     *
     * @param code        введений користувачем код
     * @param phoneNumber номер телефону, для якого перевіряється код
     */
    public void verifyCode(String code, String phoneNumber) {
        if (TextUtils.isEmpty(code)) {
            errorLiveData.setValue("Код не може бути порожнім");
            return;
        }
        // Додаткова валідація довжини коду, якщо потрібно

        isLoadingLiveData.setValue(true);
        verificationResultLiveData.setValue(null); // Скидаємо попередній результат
        errorLiveData.setValue(null);

        executor.execute(() -> {
            try {
                // --- Імітація виклику API ---
                Log.d(TAG, "Simulating API call to verify code: " + code + " for phone: " + phoneNumber);
                Thread.sleep(1500); // Імітація затримки мережі

                // TODO: Замініть це на реальний виклик API
                // boolean success = verificationApiService.verifyPhoneNumberCode(phoneNumber, code);
                boolean success = code.equals("123456"); // Заглушка: припустимо, "123456" - правильний код

                Log.d(TAG, "Verification simulation result: " + success);
                // --- Кінець імітації ---

                isLoadingLiveData.postValue(false);
                verificationResultLiveData.postValue(success);

            } catch (Exception e) { // Обробка StatusRuntimeException від gRPC або інших помилок
                Log.e(TAG, "Error verifying code: " + e.getMessage(), e);
                isLoadingLiveData.postValue(false);
                // TODO: Можна додати кращу обробку помилок API (наприклад, неправильний код vs помилка сервера)
                errorLiveData.postValue("Помилка перевірки коду: " + e.getMessage());
            }
        });
    }

    /**
     * Позначає, що помилка була оброблена (наприклад, показана користувачеві)
     */
    public void errorHandled() {
        errorLiveData.setValue(null);
    }

    // --- Getters for LiveData ---

    public LiveData<Boolean> isLoading() {
        return isLoadingLiveData;
    }

    public LiveData<Boolean> getVerificationResult() {
        return verificationResultLiveData;
    }

    public LiveData<String> getError() {
        return errorLiveData;
    }
}