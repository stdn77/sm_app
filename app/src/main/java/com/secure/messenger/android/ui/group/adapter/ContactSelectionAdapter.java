package com.secure.messenger.android.ui.group.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.secure.messenger.android.R;
import com.secure.messenger.android.data.model.User;

import java.util.ArrayList;
import java.util.List;

public class ContactSelectionAdapter extends RecyclerView.Adapter<ContactSelectionAdapter.ContactViewHolder> {

    private final List<User> contacts = new ArrayList<>();
    private OnContactSelectedListener onContactSelectedListener;

    public interface OnContactSelectedListener {
        boolean onContactSelected(User contact, int position);
    }

    public void setOnContactSelectedListener(OnContactSelectedListener listener) {
        this.onContactSelectedListener = listener;
    }

    public void setContacts(List<User> contactList) {
        contacts.clear();
        if (contactList != null) {
            contacts.addAll(contactList);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact_selection, parent, false);
        return new ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        User contact = contacts.get(position);
        holder.bind(contact);

        holder.itemView.setOnClickListener(v -> {
            if (onContactSelectedListener != null) {
                boolean isSelected = onContactSelectedListener.onContactSelected(contact, position);
                holder.checkBox.setChecked(isSelected);
            }
        });
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    static class ContactViewHolder extends RecyclerView.ViewHolder {
        private final TextView avatarText;
        private final TextView nameText;
        final CheckBox checkBox;

        ContactViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarText = itemView.findViewById(R.id.text_contact_avatar);
            nameText = itemView.findViewById(R.id.text_contact_name);
            checkBox = itemView.findViewById(R.id.checkbox_contact);
        }

        void bind(User contact) {
            nameText.setText(contact.getUsername());

            // Відображення ініціалів користувача
            String username = contact.getUsername();
            if (username != null && !username.isEmpty()) {
                avatarText.setText(String.valueOf(username.charAt(0)).toUpperCase());
            } else {
                avatarText.setText("?");
            }
        }
    }
}