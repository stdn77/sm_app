package com.secure.messenger.android.ui.group;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.secure.messenger.android.R;
import com.secure.messenger.android.ui.common.BaseActivity;

/**
 * Активність для групового чату
 */
public class GroupChatActivity extends BaseActivity {
    public static final String EXTRA_GROUP_ID = "extra_group_id";
    public static final String EXTRA_GROUP_NAME = "extra_group_name";

    private GroupChatViewModel viewModel;
    private RecyclerView recyclerView;
    private TextInputEditText messageInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        String groupId = getIntent().getStringExtra(EXTRA_GROUP_ID);
        String groupName = getIntent().getStringExtra(EXTRA_GROUP_NAME);

        if (groupId == null) {
            showToast("Помилка: неправильний ідентифікатор групи");
            finish();
            return;
        }

        // Налаштування ViewModel
        viewModel = new ViewModelProvider(this).get(GroupChatViewModel.class);
        viewModel.setGroupId(groupId);

        // Налаштування toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setupToolbar(toolbar, groupName, true);

        // Ініціалізація UI
        initViews();
        observeViewModel();

        // Завантаження повідомлень групи
        viewModel.loadMessages();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.recycler_view);
        messageInput = findViewById(R.id.message_input);

        // TODO: Налаштувати RecyclerView та адаптер для повідомлень
    }

    private void observeViewModel() {
        // TODO: Спостереження за змінами даних
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_group_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_group_info) {
            openGroupInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openGroupInfo() {
        // TODO: Відкрити інформацію про групу
    }
}