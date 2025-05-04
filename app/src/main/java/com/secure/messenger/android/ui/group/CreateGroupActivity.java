package com.secure.messenger.android.ui.group;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.secure.messenger.android.R;
import com.secure.messenger.android.data.model.User;
import com.secure.messenger.android.ui.common.BaseActivity;
import com.secure.messenger.android.ui.group.adapter.ContactSelectionAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Активність для створення нової групи
 */
public class CreateGroupActivity extends BaseActivity {

    private CreateGroupViewModel viewModel;
    private TextInputEditText editGroupName;
    private TextInputEditText editGroupDescription;
    private Switch switchReportEnabled;
    private RecyclerView recyclerViewContacts;
    private Button buttonCreateGroup;
    private ContactSelectionAdapter contactAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        // Налаштування ViewModel
        viewModel = new ViewModelProvider(this).get(CreateGroupViewModel.class);

        // Налаштування toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar, "Створення групи", true);

        // Ініціалізація UI
        initViews();
        setupContactAdapter();
        setupListeners();
        observeViewModel();

        // Завантаження контактів для вибору учасників
        viewModel.loadContacts();
    }

    private void initViews() {
        editGroupName = findViewById(R.id.edit_group_name);
        editGroupDescription = findViewById(R.id.edit_group_description);
        switchReportEnabled = findViewById(R.id.switch_report_enabled);
        recyclerViewContacts = findViewById(R.id.recycler_view_contacts);
        buttonCreateGroup = findViewById(R.id.button_create_group);
    }

    private void setupContactAdapter() {
        contactAdapter = new ContactSelectionAdapter();
        recyclerViewContacts.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewContacts.setAdapter(contactAdapter);

        // Налаштування обробника вибору контактів
        contactAdapter.setOnContactSelectedListener(new ContactSelectionAdapter.OnContactSelectedListener() {
            @Override
            public boolean onContactSelected(User contact, int position) {
                // Змінюємо стан вибору контакту
                boolean isSelected = viewModel.toggleContactSelection(contact.getId());
                return isSelected;
            }
        });
    }

    private void setupListeners() {
        buttonCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void observeViewModel() {
        // Спостереження за списком контактів
        viewModel.getContacts().observe(this, contacts -> {
            contactAdapter.setContacts(contacts);
        });

        // Спостереження за станом завантаження
        viewModel.isLoading().observe(this, isLoading -> {
            if (isLoading) {
                showProgress("Завантаження...");
            } else {
                hideProgress();
            }
        });

        // Спостереження за помилками
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                showToast(error);
                viewModel.errorHandled();
            }
        });

        // Спостереження за успішним створенням групи
        viewModel.isGroupCreated().observe(this, isCreated -> {
            if (isCreated) {
                showToast("Групу успішно створено");
                finish();
            }
        });
    }

    private void createGroup() {
        String name = editGroupName.getText().toString().trim();
        String description = editGroupDescription.getText().toString().trim();
        boolean reportEnabled = switchReportEnabled.isChecked();

        // Валідація введених даних
        if (TextUtils.isEmpty(name)) {
            editGroupName.setError("Вкажіть назву групи");
            return;
        }

        // Перевірка вибраних контактів
        int selectedCount = viewModel.getSelectedContactsCount();
        if (selectedCount == 0) {
            showToast("Виберіть хоча б одного учасника для групи");
            return;
        }

        // Показ прогресу
        showProgress("Створення групи...");

        // Створення групи
        viewModel.createGroup(name, description, reportEnabled);
    }
}