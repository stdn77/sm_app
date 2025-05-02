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
import com.secure.messenger.android.ui.common.BaseActivity;

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

        // TODO: Налаштувати RecyclerView та адаптер для контактів
    }

    private void setupListeners() {
        buttonCreateGroup.setOnClickListener(v -> createGroup());
    }

    private void observeViewModel() {
        // TODO: Спостереження за змінами даних
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

        // Отримання вибраних користувачів
        // TODO: Отримати список вибраних користувачів

        // Показ прогресу
        showProgress("Створення групи...");

        // Створення групи
        viewModel.createGroup(name, description, reportEnabled);
    }
}