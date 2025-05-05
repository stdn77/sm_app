package com.secure.messenger.android.ui.auth;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.secure.messenger.android.data.api.AuthServiceClient;
import com.secure.messenger.android.data.repository.AuthRepository;
import com.secure.messenger.android.data.model.AuthResult;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<AuthResult> authResultLiveData = new MutableLiveData<>();

    public LoginViewModel(@NonNull Application application) {
        super(application);

        // Створюємо клієнт для API автентифікації
        String serverHost = "10.0.2.2"; // localhost для емулятора
        int serverPort = 9090;
        AuthServiceClient authServiceClient = new AuthServiceClient(serverHost, serverPort);

        // Ініціалізуємо репозиторій
        this.authRepository = new AuthRepository(application.getApplicationContext(), authServiceClient);
    }

    public void login(String username, String password) {
        executor.execute(() -> {
            try {
                boolean success = authRepository.login(username, password);
                if (success) {
                    authResultLiveData.postValue(new AuthResult(true, null));
                } else {
                    authResultLiveData.postValue(new AuthResult(false, "Невірне ім'я користувача або пароль"));
                }
            } catch (Exception e) {
                authResultLiveData.postValue(new AuthResult(false, e.getMessage()));
            }
        });
    }

    public LiveData<AuthResult> getAuthResultLiveData() {
        return authResultLiveData;
    }
}