package com.secure.messenger.android.ui.group;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.secure.messenger.android.data.api.GroupServiceClient;
import com.secure.messenger.android.data.model.Group;
import com.secure.messenger.android.data.repository.GroupRepository;

import java.util.List;

/**
 * ViewModel для списку груп
 */
public class GroupViewModel extends AndroidViewModel {
    private static final String TAG = "GroupViewModel";

    private final GroupRepository groupRepository;
    private final MutableLiveData<List<Group>> groups = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    public GroupViewModel(@NonNull Application application) {
        super(application);

        // Ініціалізація клієнта та репозиторію
        // У реальному додатку використовуйте DI для ін'єкції залежностей
        String serverHost = "10.0.2.2"; // localhost для емулятора
        int serverPort = 9090;
        GroupServiceClient groupServiceClient = new GroupServiceClient(serverHost, serverPort);
        groupRepository = new GroupRepository(application, groupServiceClient);
    }

    /**
     * Завантажує список груп
     */
    public void loadGroups() {
        loading.setValue(true);
        groupRepository.getUserGroups(new GroupRepository.GroupsCallback() {
            @Override
            public void onSuccess(List<Group> groupList) {
                groups.postValue(groupList);
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
     * Оновлює список груп
     */
    public void refreshGroups() {
        loadGroups();
    }

    /**
     * Позначає помилку як оброблену
     */
    public void errorHandled() {
        error.setValue(null);
    }

    /**
     * @return LiveData зі списком груп
     */
    public LiveData<List<Group>> getGroups() {
        return groups;
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