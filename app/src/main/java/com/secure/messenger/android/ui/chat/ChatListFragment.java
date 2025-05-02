package com.secure.messenger.android.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.secure.messenger.android.R;
import com.secure.messenger.android.data.model.ChatPreview;
import com.secure.messenger.android.ui.common.adapter.BaseAdapter;
import com.secure.messenger.android.ui.contacts.ContactsActivity;

import java.util.ArrayList;

/**
 * Фрагмент із списком чатів
 */
public class ChatListFragment extends Fragment {

    private ChatListViewModel viewModel;
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private View emptyView;
    private FloatingActionButton fab;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ініціалізація ViewModel
        viewModel = new ViewModelProvider(this).get(ChatListViewModel.class);

        // Ініціалізація UI компонентів
        initViews(view);
        setupRecyclerView();
        setupSwipeRefresh();
        setupFab();

        // Спостереження за змінами даних
        observeViewModel();

        // Завантаження даних
        viewModel.loadChats();
    }

    private void initViews(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh_layout);
        emptyView = view.findViewById(R.id.empty_view);
        fab = view.findViewById(R.id.fab);
    }

    private void setupRecyclerView() {
        adapter = new ChatAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));

        // Налаштування обробника натиснень
        adapter.setOnItemClickListener((chat, position, view) -> {
            // Відкриття екрану чату
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(ChatActivity.EXTRA_CHAT_ID, chat.getId());
            intent.putExtra(ChatActivity.EXTRA_CHAT_NAME, chat.getName());
            intent.putExtra(ChatActivity.EXTRA_IS_GROUP, chat.isGroup());
            startActivity(intent);
        });
    }

    private void setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener(() -> {
            viewModel.refreshChats();
        });
    }

    private void setupFab() {
        fab.setOnClickListener(v -> {
            // Відкриття екрану контактів для початку нового чату
            Intent intent = new Intent(requireContext(), ContactsActivity.class);
            startActivity(intent);
        });
    }

    private void observeViewModel() {
        // Спостереження за списком чатів
        viewModel.getChats().observe(getViewLifecycleOwner(), chats -> {
            adapter.setItems(chats);
            updateEmptyView(chats.isEmpty());
        });

        // Спостереження за станом завантаження
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            swipeRefreshLayout.setRefreshing(isLoading);
        });

        // Спостереження за помилками
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
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

    /**
     * Адаптер для списку чатів
     */
    private static class ChatAdapter extends BaseAdapter<ChatPreview, ChatAdapter.ChatViewHolder> {

        @NonNull
        @Override
        public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = inflate(parent, R.layout.item_chat);
            return new ChatViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
            ChatPreview chat = getItem(position);
            if (chat == null) return;

            // Налаштування даних для відображення
            holder.bind(chat);

            // Налаштування обробників подій
            holder.itemView.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick(chat, position, v);
                }
            });

            holder.itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    return onItemLongClickListener.onItemLongClick(chat, position, v);
                }
                return false;
            });
        }

        @Override
        protected boolean areItemsTheSame(ChatPreview oldItem, ChatPreview newItem) {
            return oldItem.getId().equals(newItem.getId());
        }

        @Override
        protected boolean areContentsTheSame(ChatPreview oldItem, ChatPreview newItem) {
            return oldItem.getLastMessageTime().equals(newItem.getLastMessageTime()) &&
                    oldItem.getUnreadCount() == newItem.getUnreadCount() &&
                    oldItem.getLastMessage().equals(newItem.getLastMessage());
        }

        /**
         * ViewHolder для елемента чату
         */
        /**
         * ViewHolder для елемента чату
         */
        static class ChatViewHolder extends RecyclerView.ViewHolder {
            private TextView textAvatar;
            private TextView textName;
            private TextView textLastMessage;
            private TextView textTime;
            private TextView textUnreadCount;
            private View indicatorOnline;

            public ChatViewHolder(@NonNull View itemView) {
                super(itemView);
                textAvatar = itemView.findViewById(R.id.text_avatar);
                textName = itemView.findViewById(R.id.text_name);
                textLastMessage = itemView.findViewById(R.id.text_last_message);
                textTime = itemView.findViewById(R.id.text_time);
                textUnreadCount = itemView.findViewById(R.id.text_unread_count);
                indicatorOnline = itemView.findViewById(R.id.indicator_online);
            }

            public void bind(ChatPreview chat) {
                // Встановлюємо ініціали для аватара
                textAvatar.setText(chat.getInitials());

                // Встановлюємо ім'я чату
                textName.setText(chat.getName());

                // Встановлюємо останнє повідомлення
                textLastMessage.setText(chat.getLastMessage());

                // Встановлюємо час останнього повідомлення
                textTime.setText(chat.getFormattedTime());

                // Відображаємо кількість непрочитаних повідомлень
                if (chat.getUnreadCount() > 0) {
                    textUnreadCount.setVisibility(View.VISIBLE);
                    textUnreadCount.setText(String.valueOf(chat.getUnreadCount()));
                } else {
                    textUnreadCount.setVisibility(View.GONE);
                }

                // Показуємо індикатор онлайн для приватних чатів
                if (!chat.isGroup() && chat.isOnline()) {
                    indicatorOnline.setVisibility(View.VISIBLE);
                } else {
                    indicatorOnline.setVisibility(View.GONE);
                }
            }
        }
    }
}