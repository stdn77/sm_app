package com.secure.messenger.android.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.secure.messenger.android.R;
import com.secure.messenger.android.data.local.PreferenceManager;

/**
 * Фрагмент для налаштувань додатку
 */
public class SettingsFragment extends Fragment {

    private SettingsViewModel viewModel;
    private PreferenceManager preferenceManager;

    private Switch switchNotifications;
    private Switch switchDarkTheme;
    private TextView textStorageUsage;
    private TextView textCacheSize;
    private View buttonClearCache;
    private View buttonPrivacyPolicy;
    private View buttonTermsOfService;
    private View buttonAbout;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Ініціалізація ViewModel та PreferenceManager
        viewModel = new ViewModelProvider(this).get(SettingsViewModel.class);
        preferenceManager = new PreferenceManager(requireContext());

        // Ініціалізація UI компонентів
        initViews(view);
        setupListeners();
        loadSettings();
        observeViewModel();

        // Завантаження інформації про сховище
        viewModel.loadStorageInfo();
    }

    private void initViews(View view) {
        switchNotifications = view.findViewById(R.id.switch_notifications);
        switchDarkTheme = view.findViewById(R.id.switch_dark_theme);
        textStorageUsage = view.findViewById(R.id.text_storage_usage);
        textCacheSize = view.findViewById(R.id.text_cache_size);
        buttonClearCache = view.findViewById(R.id.button_clear_cache);
        buttonPrivacyPolicy = view.findViewById(R.id.button_privacy_policy);
        buttonTermsOfService = view.findViewById(R.id.button_terms_of_service);
        buttonAbout = view.findViewById(R.id.button_about);
    }

    private void setupListeners() {
        // Обробник змін налаштувань сповіщень
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setNotificationsEnabled(isChecked);
            viewModel.updateNotificationSettings(isChecked);
        });

        // Обробник змін теми
        switchDarkTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferenceManager.setThemeMode(isChecked ? 1 : 0); // 0 - світла, 1 - темна
            viewModel.updateThemeSettings(isChecked);
            // Примітка: для реального застосування теми може знадобитися перезапуск активності
        });

        // Очищення кешу
        buttonClearCache.setOnClickListener(v -> {
            viewModel.clearCache();
        });

        // Відкриття політики конфіденційності
        buttonPrivacyPolicy.setOnClickListener(v -> {
            // Відкриття екрану з політикою конфіденційності
            // TODO: Реалізувати перехід на екран політики конфіденційності
        });

        // Відкриття умов використання
        buttonTermsOfService.setOnClickListener(v -> {
            // Відкриття екрану з умовами використання
            // TODO: Реалізувати перехід на екран умов використання
        });

        // Відкриття інформації про додаток
        buttonAbout.setOnClickListener(v -> {
            // Відкриття екрану з інформацією про додаток
            // TODO: Реалізувати перехід на екран про додаток
        });
    }

    private void loadSettings() {
        // Завантаження налаштувань з преференцій
        switchNotifications.setChecked(preferenceManager.areNotificationsEnabled());
        switchDarkTheme.setChecked(preferenceManager.getThemeMode() == 1);
    }

    private void observeViewModel() {
        // Відображення інформації про сховище
        viewModel.getStorageInfo().observe(getViewLifecycleOwner(), storageInfo -> {
            if (storageInfo != null) {
                textStorageUsage.setText(storageInfo.first);
                textCacheSize.setText(storageInfo.second);
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

        // Обробка успішного очищення кешу
        viewModel.isCacheCleared().observe(getViewLifecycleOwner(), isCleared -> {
            if (isCleared) {
                Toast.makeText(requireContext(), "Кеш успішно очищено", Toast.LENGTH_SHORT).show();
                viewModel.loadStorageInfo(); // Оновлюємо інформацію про сховище
                viewModel.cacheClearHandled();
            }
        });
    }
}