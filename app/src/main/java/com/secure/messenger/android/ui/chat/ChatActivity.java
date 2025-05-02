package com.secure.messenger.android.ui.chat;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.messenger.android.R;
import com.secure.messenger.android.data.model.Message;
import com.secure.messenger.android.ui.common.BaseActivity;
import com.secure.messenger.android.ui.common.adapter.BaseAdapter;

import java.util.ArrayList;

/**
 * Активність для чату з користувачем або групою
 */
public class ChatActivity extends BaseActivity {
    private static final String TAG = "ChatActivity";

    // Константи для Intent
    public static final String EXTRA_CHAT_ID = "extra_chat_id";
    public static final String EXTRA_CHAT_NAME = "extra_chat_name";
    public static final String EXTRA_IS_GROUP = "extra_is_group";

    // UI елементи
    private RecyclerView recyclerView;
    private EditText messageInput;
    private ImageButton sendButton;
    private View attachmentButton;
    private Toolbar toolbar;

    // Дані
    private String chatId;
    private String chatName;
    private boolean isGroup;
    private ChatViewModel viewModel;
    private MessageAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Отримання даних з Intent
        Intent intent = getIntent();
        if (intent != null) {
            chatId = intent.getStringExtra(EXTRA_CHAT_ID);
            chatName = intent.getStringExtra(EXTRA_CHAT_NAME);
            isGroup = intent.getBooleanExtra(EXTRA_IS_GROUP, false);
        }

        // Перевірка наявності необхідних даних
        if (TextUtils.isEmpty(chatId) || TextUtils.isEmpty(chatName)) {
            Log.e(TAG, "Missing required chat data");
            finish();
            return;
        }

        // Ініціалізація ViewModel
        viewModel = new ViewModelProvider(this).get(ChatViewModel.class);
        viewModel.init(chatId, isGroup);

        // Ініціалізація UI
        initViews();
        setupToolbar();
        setupRecyclerView();
        setupClickListeners();

        // Спостереження за даними
        observeViewModel();

        // Завантаження повідомлень
        loadMessages();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recycler_view);
        messageInput = findViewById(R.id.edit_message);
        sendButton = findViewById(R.id.button_send);
        attachmentButton = findViewById(R.id.button_attach);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(chatName);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    private void setupRecyclerView() {
        adapter = new MessageAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Прокрутка знизу вгору
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        // Обробка натискання кнопки надсилання
        sendButton.setOnClickListener(v -> sendMessage());

        // Обробка натискання кнопки вкладення
        attachmentButton.setOnClickListener(v -> showAttachmentOptions());
    }

    private void observeViewModel() {
        // Спостереження за списком повідомлень
        viewModel.getMessages().observe(this, messages -> {
            adapter.setItems(messages);
            scrollToBottom();
        });

        // Спостереження за статусом надсилання повідомлення
        viewModel.getSendMessageStatus().observe(this, success -> {
            if (success != null) {
                if (success) {
                    messageInput.setText("");
                } else {
                    Toast.makeText(this, "Не вдалося надіслати повідомлення", Toast.LENGTH_SHORT).show();
                }
                viewModel.resetSendMessageStatus();
            }
        });

        // Спостереження за помилками
        viewModel.getError().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
                viewModel.resetError();
            }
        });
    }

    private void loadMessages() {
        viewModel.loadMessages();
    }

    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (!TextUtils.isEmpty(messageText)) {
            viewModel.sendTextMessage(messageText);
        }
    }

    private void showAttachmentOptions() {
        // Показуємо меню вибору типу вкладення (фото, документ, голосове повідомлення)
        // TODO: Реалізувати діалог вибору типу вкладення
        Toast.makeText(this, "Функція додавання вкладень у розробці", Toast.LENGTH_SHORT).show();
    }

    private void scrollToBottom() {
        if (adapter.getItemCount() > 0) {
            recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
        }
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
     * Адаптер для відображення повідомлень у чаті
     */
    private class MessageAdapter extends BaseAdapter<Message, MessageAdapter.MessageViewHolder> {
        private static final int VIEW_TYPE_MY_MESSAGE = 0;
        private static final int VIEW_TYPE_OTHER_MESSAGE = 1;
        private String currentUserId;

        MessageAdapter() {
            currentUserId = preferenceManager.getUserId();
        }

        @Override
        public int getItemViewType(int position) {
            Message message = getItem(position);
            if (message != null && message.getSenderId().equals(currentUserId)) {
                return VIEW_TYPE_MY_MESSAGE;
            } else {
                return VIEW_TYPE_OTHER_MESSAGE;
            }
        }

        @NonNull
        @Override
        public MessageViewHolder onCreateViewHolder(@NonNull android.view.ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_MY_MESSAGE) {
                View view = inflate(parent, R.layout.item_message_sent);
                return new MessageViewHolder(view);
            } else {
                View view = inflate(parent, R.layout.item_message_received);
                return new MessageViewHolder(view);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
            Message message = getItem(position);
            if (message != null) {
                holder.bind(message);
            }
        }

        @Override
        protected boolean areItemsTheSame(Message oldItem, Message newItem) {
            return oldItem.getId() != null && oldItem.getId().equals(newItem.getId());
        }

        @Override
        protected boolean areContentsTheSame(Message oldItem, Message newItem) {
            return oldItem.getStatus() == newItem.getStatus();
        }

        /**
         * ViewHolder для відображення повідомлення
         */
        class MessageViewHolder extends RecyclerView.ViewHolder {
            // TODO: Додайте посилання на UI елементи повідомлення
            // TextView messageText, timeText
            // ImageView statusIcon, etc.

            MessageViewHolder(@NonNull View itemView) {
                super(itemView);
                // TODO: Ініціалізуйте UI елементи
            }

            void bind(Message message) {
                // TODO: Встановіть дані повідомлення у UI елементи
                // messageText.setText(message.getTextContent());
                // timeText.setText(message.getFormattedTime());
                // Відображення статусу повідомлення (надіслано, доставлено, прочитано)
                // Встановлення слухачів подій, якщо потрібно
            }
        }
    }
}