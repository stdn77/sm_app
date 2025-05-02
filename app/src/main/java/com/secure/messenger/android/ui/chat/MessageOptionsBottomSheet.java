package com.secure.messenger.android.ui.chat;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.secure.messenger.android.R;
import com.secure.messenger.android.data.model.Message;

/**
 * Діалог з опціями для повідомлення
 */
public class MessageOptionsBottomSheet extends BottomSheetDialogFragment {

    private static final String ARG_MESSAGE_ID = "message_id";
    private static final String ARG_MESSAGE_TEXT = "message_text";
    private static final String ARG_IS_MY_MESSAGE = "is_my_message";

    private String messageId;
    private String messageText;
    private boolean isMyMessage;

    private MessageOptionsListener listener;

    /**
     * Інтерфейс для слухача подій опцій повідомлення
     */
    public interface MessageOptionsListener {
        void onCopyMessage(String messageId);
        void onDeleteMessage(String messageId);
        void onReplyToMessage(String messageId);
        void onForwardMessage(String messageId);
    }

    /**
     * Створює новий екземпляр діалогу з опціями повідомлення
     *
     * @param messageId ідентифікатор повідомлення
     * @param messageText текст повідомлення
     * @param isMyMessage чи є повідомлення відправленим поточним користувачем
     * @return новий екземпляр діалогу
     */
    public static MessageOptionsBottomSheet newInstance(String messageId, String messageText, boolean isMyMessage) {
        MessageOptionsBottomSheet fragment = new MessageOptionsBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_MESSAGE_ID, messageId);
        args.putString(ARG_MESSAGE_TEXT, messageText);
        args.putBoolean(ARG_IS_MY_MESSAGE, isMyMessage);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            messageId = getArguments().getString(ARG_MESSAGE_ID);
            messageText = getArguments().getString(ARG_MESSAGE_TEXT);
            isMyMessage = getArguments().getBoolean(ARG_IS_MY_MESSAGE);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof MessageOptionsListener) {
            listener = (MessageOptionsListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement MessageOptionsListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_message_options, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Налаштування опцій
        View copyOption = view.findViewById(R.id.option_copy);
        View replyOption = view.findViewById(R.id.option_reply);
        View forwardOption = view.findViewById(R.id.option_forward);
        View deleteOption = view.findViewById(R.id.option_delete);

        // Опція копіювання доступна для всіх повідомлень
        copyOption.setOnClickListener(v -> {
            copyMessageToClipboard();
            if (listener != null) {
                listener.onCopyMessage(messageId);
            }
            dismiss();
        });

        // Опція відповіді доступна для всіх повідомлень
        replyOption.setOnClickListener(v -> {
            if (listener != null) {
                listener.onReplyToMessage(messageId);
            }
            dismiss();
        });

        // Опція пересилання доступна для всіх повідомлень
        forwardOption.setOnClickListener(v -> {
            if (listener != null) {
                listener.onForwardMessage(messageId);
            }
            dismiss();
        });

        // Опція видалення доступна тільки для власних повідомлень
        if (isMyMessage) {
            deleteOption.setVisibility(View.VISIBLE);
            deleteOption.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteMessage(messageId);
                }
                dismiss();
            });
        } else {
            deleteOption.setVisibility(View.GONE);
        }
    }

    /**
     * Копіює текст повідомлення в буфер обміну
     */
    private void copyMessageToClipboard() {
        if (getContext() != null && messageText != null) {
            ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Message", messageText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), R.string.message_copied, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}