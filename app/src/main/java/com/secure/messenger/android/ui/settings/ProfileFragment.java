package com.secure.messenger.android.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.secure.messenger.android.R;
import com.secure.messenger.android.data.local.TokenManager;
import com.secure.messenger.android.data.model.User;

/**
 * Фрагмент для відображення та редагування профілю користувача
 */
public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TokenManager tokenManager;

    private TextView textUsername;
    private TextView textPhoneNumber;
    private TextView textStatus;
    private Button buttonEditProfile;
    private View profileContent;
    private View editProfileForm;
    private EditText editUsername;
    private EditText editStatus;
    private Button buttonSaveProfile;
    private Button buttonCancel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ініціалізація ViewModel та TokenManager
        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);
        tokenManager = new TokenManager(requireContext());

        // Ініціалізація UI компонентів
        initViews(view);
        setupListeners();
        observeViewModel();

        // Завантаження профілю
        viewModel.loadUserProfile();
    }

    private void initViews(View view) {
        // Ініціалізація компонентів відображення профілю
        profileContent = view.findViewById(R.id.layout_profile_content);
        textUsername = view.findViewById(R.id.text_username);
        textPhoneNumber = view.findViewById(R.id.text_phone_number);
        textStatus = view.findViewById(R.id.text_status);
        buttonEditProfile = view.findViewById(R.id.button_edit_profile);

        // Ініціалізація компонентів редагування профілю
        editProfileForm = view.findViewById(R.id.layout_edit_profile);
        editUsername = view.findViewById(R.id.edit_username);
        editStatus = view.findViewById(R.id.edit_status);
        buttonSaveProfile = view.findViewById(R.id.button_save_profile);
        buttonCancel = view.findViewById(R.id.button_cancel);

        // Початково форма редагування прихована
        profileContent.setVisibility(View.VISIBLE);
        editProfileForm.setVisibility(View.GONE);
    }

    private void setupListeners() {
        // Перехід до режиму редагування
        buttonEditProfile.setOnClickListener(v -> {
            // Заповнюємо поля редагування поточними даними
            User currentUser = viewModel.getUserProfile().getValue();
            if (currentUser != null) {
                editUsername.setText(currentUser.getUsername());
                editStatus.setText(currentUser.getStatus());
            }

            // Показуємо форму редагування
            profileContent.setVisibility(View.GONE);
            editProfileForm.setVisibility(View.VISIBLE);
        });

        // Збереження змін профілю
        buttonSaveProfile.setOnClickListener(v -> {
            String username = editUsername.getText().toString().trim();
            String status = editStatus.getText().toString().trim();

            // Валідація даних
            if (username.isEmpty()) {
                editUsername.setError("Ім'я користувача не може бути порожнім");
                return;
            }

            // Збереження змін
            viewModel.updateUserProfile(username, status);
        });

        // Скасування редагування
        buttonCancel.setOnClickListener(v -> {
            // Повертаємось до перегляду профілю
            profileContent.setVisibility(View.VISIBLE);
            editProfileForm.setVisibility(View.GONE);
        });
    }

    private void observeViewModel() {
        // Відображення даних профілю
        viewModel.getUserProfile().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                textUsername.setText(user.getUsername());
                textPhoneNumber.setText(user.getPhoneNumber());
                textStatus.setText(user.getStatus() != null ? user.getStatus() : "Статус не встановлено");
            }
        });

        // Відображення стану завантаження
        viewModel.isLoading().observe(getViewLifecycleOwner(), isLoading -> {
            // Тут можна показати прогрес-бар або заблокувати UI під час завантаження
        });

        // Обробка помилок
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show();
                viewModel.errorHandled();
            }
        });

        // Обробка успішного оновлення профілю
        viewModel.isProfileUpdated().observe(getViewLifecycleOwner(), isUpdated -> {
            if (isUpdated) {
                // Повертаємось до перегляду профілю
                profileContent.setVisibility(View.VISIBLE);
                editProfileForm.setVisibility(View.GONE);

                Toast.makeText(requireContext(), "Профіль успішно оновлено", Toast.LENGTH_SHORT).show();
                viewModel.profileUpdateHandled();
            }
        });
    }
}