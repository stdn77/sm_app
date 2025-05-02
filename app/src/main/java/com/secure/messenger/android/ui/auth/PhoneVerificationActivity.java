package com.secure.messenger.android.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputEditText;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import com.secure.messenger.android.R;

public class PhoneVerificationActivity extends AppCompatActivity {

    private PhoneVerificationViewModel viewModel;
    private TextInputEditText editPhone;
    private Button buttonSendCode;
    private PhoneNumberUtil phoneNumberUtil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_verification);

        // Ініціалізація ViewModel
        viewModel = new ViewModelProvider(this).get(PhoneVerificationViewModel.class);

        // Ініціалізація UI компонентів
        editPhone = findViewById(R.id.editPhone);
        buttonSendCode = findViewById(R.id.buttonSendCode);

        // Ініціалізація утиліти для валідації телефонів
        phoneNumberUtil = PhoneNumberUtil.getInstance();

        // Налаштування слухачів
        buttonSendCode.setOnClickListener(v -> validateAndSendCode());

        // Спостереження за статусом відправки коду
        viewModel.getCodeSentLiveData().observe(this, success -> {
            if (success) {
                navigateToCodeVerification();
            } else {
                showError("Не вдалося надіслати код. Спробуйте ще раз.");
            }
        });
    }

    private void validateAndSendCode() {
        String phoneNumber = editPhone.getText().toString().trim();

        if (TextUtils.isEmpty(phoneNumber)) {
            editPhone.setError("Вкажіть номер телефону");
            return;
        }

        // Валідація формату телефону
        try {
            Phonenumber.PhoneNumber number = phoneNumberUtil.parse(phoneNumber, "UA");
            if (!phoneNumberUtil.isValidNumber(number)) {
                editPhone.setError("Невірний формат номера телефону");
                return;
            }

            // Перетворення номера у міжнародний формат
            String formattedNumber = phoneNumberUtil.format(number, PhoneNumberUtil.PhoneNumberFormat.E164);

            // Відправка коду підтвердження
            viewModel.sendVerificationCode(formattedNumber);

        } catch (NumberParseException e) {
            editPhone.setError("Невірний формат номера телефону");
        }
    }

    private void navigateToCodeVerification() {
        String phoneNumber = editPhone.getText().toString().trim();
        Intent intent = new Intent(this, CodeVerificationActivity.class);
        intent.putExtra("PHONE_NUMBER", phoneNumber);
        startActivity(intent);
    }

    private void showError(String errorMessage) {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }
}