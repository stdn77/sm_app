package com.secure.messenger.android.ui.contacts;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.secure.messenger.android.R;
import com.secure.messenger.android.data.model.User;
import com.secure.messenger.android.ui.common.BaseActivity;
import com.secure.messenger.android.ui.common.adapter.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Активність для відображення та вибору контактів
 */
public class ContactsActivity extends BaseActivity {

    private ContactsViewModel viewModel;
    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private ContactAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts);

        // Ініціалізація ViewModel
        viewModel = new ViewModelProvider(this).get(ContactsViewModel.class);

        // Ініціалізація UI компонентів
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupSwipeRefresh();

        // Спостереження за змінами даних
        observeViewModel();

        // Завантаження контактів
        viewModel.loadContacts();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swipe_refresh_layout);
        emptyView = findViewById(R.id.empty_view);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.title_contacts);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView() {
        adapter = new ContactAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));

        // Налаштування обробника натиснень
        adapter.setOnItemClickListener((contact, position, view) -> {
            // Відкриття чату з контактом
            startChatWithContact(contact);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshContacts();
        });
    }

    private void observeViewModel() {
        // Спостереження за списком контактів
        viewModel.getContacts().observe(this, contacts -> {
            adapter.setItems(contacts);
            updateEmptyView(contacts.isEmpty());
        });

        // Спостереження за станом завантаження
        viewModel.isLoading().observe(this, isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // Спостереження за помилками
        viewModel.getError().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.errorHandled();
            }
        });
    }

    private void updateEmptyView(boolean isEmpty) {
        if (isEmpty) {
            emptyView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            emptyView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void startChatWithContact(User contact) {
        // TODO: Реалізувати створення чату з контактом
        // Тут буде код для створення нового чату або переходу до існуючого
        Toast.makeText(this, "Чат з " + contact.getUsername(), Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Адаптер для списку контактів
     */
    private static class ContactAdapter extends BaseAdapter<User, ContactAdapter.ContactViewHolder> {

        @NonNull
        @Override
        public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflate(parent, R.layout.item_contact);
            return new ContactViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
            User contact = getItem(position);
            if (contact == null) return;

            // Налаштування даних для відображення
            holder.bind(contact);

            // Налаштування обробників подій
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(contact, position, v);
                }
            });
        }

        @Override
        protected boolean areItemsTheSame(User oldItem, User newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        protected boolean areContentsTheSame(User oldItem, User newItem) {
            return oldItem.getUsername().equals(newItem.getUsername()) &&
                    oldItem.getStatus().equals(newItem.getStatus());
        }

        /**
         * ViewHolder для елемента контакту
         */
        static class ContactViewHolder extends RecyclerView.ViewHolder {
            // Тут будуть оголошення UI компонентів

            public ContactViewHolder(@NonNull View itemView) {
                super(itemView);
                // Ініціалізація UI компонентів
            }

            public void bind(User contact) {
                // Налаштування відображення даних контакту
            }
        }
    }
}