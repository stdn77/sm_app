package com.secure.messenger.android;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import com.secure.messenger.android.data.local.PreferenceManager;
import com.secure.messenger.android.util.SecurityUtils;

/**
 * Основний клас додатку.
 * Ініціалізує компоненти, необхідні для роботи додатку.
 */
public class SecureMessengerApp extends Application {

    private static final String TAG = "SecureMessengerApp";
    private static final String MESSAGES_CHANNEL_ID = "messages_channel";
    private static final String VOICE_CHANNEL_ID = "voice_channel";

    private static SecureMessengerApp instance;
    private PreferenceManager preferenceManager;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        // Ініціалізація менеджера налаштувань
        preferenceManager = new PreferenceManager(this);

        // Ініціалізація криптографічних компонентів
        initializeCrypto();

        // Створення каналів сповіщень
        createNotificationChannels();

        Log.i(TAG, "SecureMessenger App initialized");
    }

    /**
     * Отримання екземпляру додатку
     * @return екземпляр класу Application
     */
    public static SecureMessengerApp getInstance() {
        return instance;
    }

    /**
     * Отримання менеджера налаштувань
     * @return екземпляр PreferenceManager
     */
    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    /**
     * Ініціалізує криптографічні компоненти
     */
    private void initializeCrypto() {
        try {
            SecurityUtils securityUtils = new SecurityUtils(this);
            if (!preferenceManager.isKeysGenerated()) {
                securityUtils.generateAndStoreKeys();
                preferenceManager.setKeysGenerated(true);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing crypto: " + e.getMessage(), e);
        }
    }

    /**
     * Створює канали сповіщень для Android 8.0 (API 26) і вище
     */
    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);

            // Канал для звичайних повідомлень
            NotificationChannel messagesChannel = new NotificationChannel(
                    MESSAGES_CHANNEL_ID,
                    "Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            messagesChannel.setDescription("Notifications for new messages");

            // Канал для голосових повідомлень
            NotificationChannel voiceChannel = new NotificationChannel(
                    VOICE_CHANNEL_ID,
                    "Voice Messages",
                    NotificationManager.IMPORTANCE_HIGH
            );
            voiceChannel.setDescription("Notifications for voice calls and messages");
            voiceChannel.setSound(null, null); // Без звуку, оскільки це голосова активність

            notificationManager.createNotificationChannel(messagesChannel);
            notificationManager.createNotificationChannel(voiceChannel);

            Log.i(TAG, "Notification channels created");
        }
    }

    /**
     * Отримує ID каналу сповіщень для повідомлень
     * @return ID каналу
     */
    public static String getMessagesChannelId() {
        return MESSAGES_CHANNEL_ID;
    }

    /**
     * Отримує ID каналу сповіщень для голосу
     * @return ID каналу
     */
    public static String getVoiceChannelId() {
        return VOICE_CHANNEL_ID;
    }
}