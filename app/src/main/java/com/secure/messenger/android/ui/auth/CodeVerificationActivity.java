package com.secure.messenger.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.secure.messenger.android.R;
// Припускаємо, що наступний крок - RegisterActivity
import com.secure.messenger.android.ui.auth.RegisterActivity;

public class CodeVerificationActivity extends AppCompatActivity {

    public static final String EXTRA_PHONE_NUMBER = "PHONE_NUMBER";

    private CodeVerificationViewModel viewModel;
    private TextInputLayout layoutCode;
    private TextInputEditText editCode;
    private Button buttonVerify;
    private ProgressBar progressBar;
    private TextView textPhoneNumberInfo;

    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Встановіть ваш макет
        setContentView(R.layout.activity_code_verification);

        // Отримуємо номер телефону з попередньої активності
        phoneNumber = getIntent().getStringExtra(EXTRA_PHONE_NUMBER);
        if (TextUtils.isEmpty(phoneNumber)) {
            showToast("Помилка: номер телефону не передано.");
            finish(); // Закриваємо активність, якщо немає номера
            return;
        }

        // Ініціалізація ViewModel
        viewModel = new ViewModelProvider(this).get(CodeVerificationViewModel.class);

        // Ініціалізація UI
        initViews();
        setupListeners();

        // Відображаємо номер телефону, на який надіслано код
        // TODO: Переконайтесь, що у вас є textPhoneNumberInfo в макеті
        if (textPhoneNumberInfo != null) {
            textPhoneNumberInfo.setText(getString(R.string.code_sent_to_info, phoneNumber)); // Потрібен рядок R.string.code_sent_to_info
        }


        // Спостереження за ViewModel
        observeViewModel();
    }

    private void initViews() {
        // TODO: Замініть R.id.* на реальні ID з вашого макета activity_code_verification.xml
        layoutCode = findViewById(R.id.layout_code); // Приклад ID
        editCode = findViewById(R.id.edit_code);     // Приклад ID
        buttonVerify = findViewById(R.id.button_verify); // Приклад ID
        progressBar = findViewById(R.id.progress_bar); // Приклад ID
        textPhoneNumberInfo = findViewById(R.id.text_phone_number_info); // Приклад ID (опціонально)

        // TODO: Налаштуйте Toolbar, якщо потрібно
        // Toolbar toolbar = findViewById(R.id.toolbar);
        // setSupportActionBar(toolbar);
        // getSupportActionBar().setTitle(R.string.title_code_verification); // Потрібен рядок
        // getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Кнопка "Назад"
    }

    private void setupListeners() {
        buttonVerify.setOnClickListener(v -> attemptVerification());

        // Очищення помилки при фокусі
        if (editCode != null && layoutCode != null) {
            editCode.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) layoutCode.setError(null);
            });
        }
    }

    private void observeViewModel() {
        viewModel.isLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            buttonVerify.setEnabled(!isLoading);
            if (editCode != null) editCode.setEnabled(!isLoading);
        });

        viewModel.getVerificationResult().observe(this, success -> {
            if (success != null) { // Перевіряємо, чи є результат (не null)
                if (success) {
                    showToast(getString(R.string.verification_successful)); // Потрібен рядок
                    navigateToRegister();
                } else {
                    if (layoutCode != null) layoutCode.setError(getString(R.string.error_invalid_code)); // Потрібен рядок
                    showToast(getString(R.string.error_invalid_code));
                }
            }
        });

        viewModel.getError().observe(this, error -> {
            if (error != null) {
                showToast(getString(R.string.error_verifying_code) + ": " + error); // Потрібен рядок
                viewModel.errorHandled(); // Повідомляємо ViewModel, що помилка оброблена
            }
        });
    }

    private void attemptVerification() {
        String code = "";
        if (editCode != null) {
            code = editCode.getText().toString().trim();
        }

        // Проста валідація
        if (TextUtils.isEmpty(code)) {
            if (layoutCode != null) layoutCode.setError(getString(R.string.error_empty_code)); // Потрібен рядок
            return;
        }
        // Можна додати перевірку довжини коду, наприклад:
        // if (code.length() != 6) {
        //    if (layoutCode != null) layoutCode.setError(getString(R.string.error_invalid_code_length)); // Потрібен рядок
        //    return;
        // }


        // Скидаємо помилку перед новою спробою
        if (layoutCode != null) layoutCode.setError(null);

        // Викликаємо метод ViewModel
        viewModel.verifyCode(code, phoneNumber);
    }

    private void navigateToRegister() {
        Intent intent = new Intent(this, RegisterActivity.class);
        // Передаємо підтверджений номер телефону далі
        intent.putExtra(RegisterActivity.EXTRA_VERIFIED_PHONE_NUMBER, phoneNumber); // Потрібно додати константу EXTRA_VERIFIED_PHONE_NUMBER в RegisterActivity
        // Очищаємо стек, щоб користувач не повернувся на екрани верифікації
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Закриваємо поточну активність
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    // Обробка кнопки "Назад" на Toolbar (якщо є)
    // @Override
    // public boolean onSupportNavigateUp() {
    //     onBackPressed();
    //     return true;
    // }
}