package com.secure.messenger.android.ui.auth;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.secure.messenger.android.data.repository.AuthRepository;
import com.secure.messenger.android.data.model.AuthResult;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class LoginViewModel extends ViewModel {

    private final AuthRepository authRepository;
    private final Executor executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<AuthResult> authResultLiveData = new MutableLiveData<>();

    public LoginViewModel() {
        // У реальному додатку використовуйте Dependency Injection
        this.authRepository = new AuthRepository();
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