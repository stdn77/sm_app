package com.secure.messenger.android.ui.chat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.messenger.android.R;
import com.secure.messenger.android.data.model.Message;
import com.secure.messenger.android.ui.common.adapter.BaseAdapter;

/**
 * Адаптер для відображення повідомлень у RecyclerView
 */
public class MessageAdapter extends BaseAdapter<Message, RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_MY_MESSAGE = 0;
    private static final int VIEW_TYPE_OTHER_MESSAGE = 1;

    private final String currentUserId;
    private final boolean isGroupChat;

    /**
     * Конструктор адаптера
     *
     * @param currentUserId ID поточного користувача
     * @param isGroupChat чи є чат груповим
     */
    public MessageAdapter(String currentUserId, boolean isGroupChat) {
        this.currentUserId = currentUserId;
        this.isGroupChat = isGroupChat;
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
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_MY_MESSAGE) {
            View view = inflate(parent, R.layout.item_message_sent);
            return new SentMessageViewHolder(view);
        } else {
            View view = inflate(parent, R.layout.item_message_received);
            return new ReceivedMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);
        if (message != null) {
            if (holder instanceof SentMessageViewHolder) {
                ((SentMessageViewHolder) holder).bind(message);
            } else if (holder instanceof ReceivedMessageViewHolder) {
                ((ReceivedMessageViewHolder) holder).bind(message);
            }
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
     * ViewHolder для відправлених повідомлень
     */
    class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView messageText;
        private final TextView timeText;
        private final ImageView statusIcon;

        SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.text_message);
            timeText = itemView.findViewById(R.id.text_time);
            statusIcon = itemView.findViewById(R.id.image_status);
        }

        void bind(Message message) {
            // Встановлюємо текст повідомлення
            messageText.setText(message.getTextContent());

            // Встановлюємо час повідомлення
            timeText.setText(message.getFormattedTime());

            // Встановлюємо статус повідомлення
            updateMessageStatus(message.getStatus());

            // Налаштовуємо слухач для довгого натискання
            itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    return onItemLongClickListener.onItemLongClick(message, getAdapterPosition(), v);
                }
                return false;
            });
        }

        /**
         * Оновлює іконку статусу повідомлення
         *
         * @param status статус повідомлення
         */
        private void updateMessageStatus(Message.MessageStatus status) {
            if (status == null) {
                statusIcon.setVisibility(View.GONE);
                return;
            }

            statusIcon.setVisibility(View.VISIBLE);

            switch (status) {
                case SENDING:
                    // Повідомлення відправляється
                    statusIcon.setImageResource(R.drawable.ic_sending);
                    break;
                case SENT:
                    // Повідомлення надіслане
                    statusIcon.setImageResource(R.drawable.ic_check);
                    break;
                case DELIVERED:
                    // Повідомлення доставлене
                    statusIcon.setImageResource(R.drawable.ic_double_check);
                    break;
                case READ:
                    // Повідомлення прочитане
                    statusIcon.setImageResource(R.drawable.ic_double_check);
                    statusIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.colorAccent));
                    break;
                case FAILED:
                    // Помилка відправки повідомлення
                    statusIcon.setImageResource(R.drawable.ic_error);
                    statusIcon.setColorFilter(itemView.getContext().getResources().getColor(R.color.colorError));
                    break;
            }
        }
    }

    /**
     * ViewHolder для отриманих повідомлень
     */
    class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView senderNameText;
        private final TextView messageText;
        private final TextView timeText;
        private final ImageView avatarImage;

        ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderNameText = itemView.findViewById(R.id.text_sender_name);
            messageText = itemView.findViewById(R.id.text_message);
            timeText = itemView.findViewById(R.id.text_time);
            avatarImage = itemView.findViewById(R.id.image_avatar);
        }

        void bind(Message message) {
            // Встановлюємо текст повідомлення
            messageText.setText(message.getTextContent());

            // Встановлюємо час повідомлення
            timeText.setText(message.getFormattedTime());

            // Показуємо ім'я відправника в групових чатах
            if (isGroupChat) {
                senderNameText.setVisibility(View.VISIBLE);
                senderNameText.setText(message.getSenderName());
            } else {
                senderNameText.setVisibility(View.GONE);
            }

            // Налаштовуємо аватар (заглушка)
            // TODO: Завантажити реальний аватар

            // Налаштовуємо слухач для довгого натискання
            itemView.setOnLongClickListener(v -> {
                if (onItemLongClickListener != null) {
                    return onItemLongClickListener.onItemLongClick(message, getAdapterPosition(), v);
                }
                return false;
            });
        }
    }
}