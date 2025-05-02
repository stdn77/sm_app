package com.secure.messenger.android.data.local;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.secure.messenger.android.util.CryptoUtils;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * Клас для безпечного зберігання та управління криптографічними ключами
 */
public class KeyManager {
    private static final String TAG = "KeyManager";

    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEYPAIR_ALIAS = "secure_messenger_keypair";
    private static final String SYMMETRIC_KEY_ALIAS = "secure_messenger_symmetric_key";

    private final Context context;
    private final KeyStore keyStore;
    private final Map<String, SecretKey> groupKeyCache;

    /**
     * Конструктор
     *
     * @param context контекст додатку
     */
    public KeyManager(Context context) {
        this.context = context;
        this.groupKeyCache = new HashMap<>();

        KeyStore ks = null;
        try {
            ks = KeyStore.getInstance(ANDROID_KEYSTORE);
            ks.load(null);
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException e) {
            Log.e(TAG, "Error initializing KeyStore: " + e.getMessage(), e);
        }
        this.keyStore = ks;
    }

    /**
     * Ініціалізує ключі шифрування при першому запуску
     *
     * @return true, якщо ключі успішно ініціалізовані
     */
    public boolean initializeKeys() {
        try {
            PreferenceManager preferenceManager = new PreferenceManager(context);
            if (!preferenceManager.isKeysGenerated()) {
                generateAsymmetricKeyPair();
                generateSymmetricKey();
                preferenceManager.setKeysGenerated(true);
                Log.i(TAG, "Cryptographic keys generated successfully");
                return true;
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error initializing keys: " + e.getMessage(), e);
            return false;
        }
    }

    /**
     * Генерує пару асиметричних ключів (для E2E шифрування)
     *
     * @return згенерована пара ключів
     * @throws Exception якщо виникла помилка
     */
    public KeyPair generateAsymmetricKeyPair() throws Exception {
        if (keyStore == null) {
            throw new IllegalStateException("KeyStore not initialized");
        }

        return CryptoUtils.generateRSAKeyPair();
    }

    /**
     * Генерує симетричний ключ (для шифрування локальних даних)
     *
     * @return згенерований симетричний ключ
     * @throws Exception якщо виникла помилка
     */
    public SecretKey generateSymmetricKey() throws Exception {
        if (keyStore == null) {
            throw new IllegalStateException("KeyStore not initialized");
        }

        KeyGenerator keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE);

        keyGenerator.init(
                new KeyGenParameterSpec.Builder(
                        SYMMETRIC_KEY_ALIAS,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build());

        return keyGenerator.generateKey();
    }

    /**
     * Отримує публічний ключ користувача
     *
     * @return публічний ключ
     * @throws Exception якщо виникла помилка
     */
    public PublicKey getPublicKey() throws Exception {
        if (keyStore == null) {
            throw new IllegalStateException("KeyStore not initialized");
        }

        return CryptoUtils.getRSAPublicKey();
    }

    /**
     * Отримує приватний ключ користувача
     *
     * @return приватний ключ
     * @throws Exception якщо виникла помилка
     */
    public PrivateKey getPrivateKey() throws Exception {
        if (keyStore == null) {
            throw new IllegalStateException("KeyStore not initialized");
        }

        return CryptoUtils.getRSAPrivateKey();
    }

    /**
     * Отримує симетричний ключ для шифрування локальних даних
     *
     * @return симетричний ключ
     * @throws Exception якщо виникла помилка
     */
    public SecretKey getSymmetricKey() throws Exception {
        if (keyStore == null) {
            throw new IllegalStateException("KeyStore not initialized");
        }

        KeyStore.SecretKeyEntry secretKeyEntry = (KeyStore.SecretKeyEntry) keyStore.getEntry(
                SYMMETRIC_KEY_ALIAS, null);

        if (secretKeyEntry == null) {
            // Якщо ключа немає, генеруємо новий
            return generateSymmetricKey();
        }

        return secretKeyEntry.getSecretKey();
    }

    /**
     * Додає ключ групи до кешу
     *
     * @param groupId ідентифікатор групи
     * @param groupKey ключ групи
     */
    public void addGroupKey(String groupId, SecretKey groupKey) {
        groupKeyCache.put(groupId, groupKey);
    }

    /**
     * Отримує ключ групи з кешу
     *
     * @param groupId ідентифікатор групи
     * @return ключ групи або null, якщо ключа немає в кеші
     */
    public SecretKey getGroupKey(String groupId) {
        return groupKeyCache.get(groupId);
    }

    /**
     * Видаляє ключ групи з кешу
     *
     * @param groupId ідентифікатор групи
     */
    public void removeGroupKey(String groupId) {
        groupKeyCache.remove(groupId);
    }

    /**
     * Шифрує дані для користувача
     *
     * @param data дані для шифрування
     * @param publicKey публічний ключ отримувача
     * @return зашифровані дані
     * @throws Exception якщо виникла помилка
     */
    public byte[] encryptForUser(byte[] data, PublicKey publicKey) throws Exception {
        return CryptoUtils.encryptWithRSA(data, publicKey);
    }

    /**
     * Розшифровує дані, отримані від іншого користувача
     *
     * @param encryptedData зашифровані дані
     * @return розшифровані дані
     * @throws Exception якщо виникла помилка
     */
    public byte[] decryptFromUser(byte[] encryptedData) throws Exception {
        PrivateKey privateKey = getPrivateKey();
        return CryptoUtils.decryptWithRSA(encryptedData, privateKey);
    }

    /**
     * Шифрує дані для групи
     *
     * @param data дані для шифрування
     * @param groupId ідентифікатор групи
     * @return зашифровані дані
     * @throws Exception якщо виникла помилка
     */
    public byte[] encryptForGroup(byte[] data, String groupId) throws Exception {
        SecretKey groupKey = getGroupKey(groupId);
        if (groupKey == null) {
            throw new IllegalStateException("Group key not found for group: " + groupId);
        }
        return CryptoUtils.encryptWithAES(data, groupKey);
    }

    /**
     * Розшифровує дані, отримані від групи
     *
     * @param encryptedData зашифровані дані
     * @param groupId ідентифікатор групи
     * @return розшифровані дані
     * @throws Exception якщо виникла помилка
     */
    public byte[] decryptFromGroup(byte[] encryptedData, String groupId) throws Exception {
        SecretKey groupKey = getGroupKey(groupId);
        if (groupKey == null) {
            throw new IllegalStateException("Group key not found for group: " + groupId);
        }
        return CryptoUtils.decryptWithAES(encryptedData, groupKey);
    }

    /**
     * Генерує ключ для нової групи
     *
     * @param groupId ідентифікатор групи
     * @return згенерований ключ групи
     * @throws Exception якщо виникла помилка
     */
    public SecretKey generateGroupKey(String groupId) throws Exception {
        SecretKey groupKey = CryptoUtils.generateAESKey();
        addGroupKey(groupId, groupKey);
        return groupKey;
    }

    /**
     * Зберігає зашифрований ключ групи
     *
     * @param groupId ідентифікатор групи
     * @param encryptedKey зашифрований ключ групи
     * @throws Exception якщо виникла помилка
     */
    public void saveEncryptedGroupKey(String groupId, byte[] encryptedKey) throws Exception {
        // TODO: Зберігати зашифрований ключ групи в базі даних або SharedPreferences
    }

    /**
     * Очищає всі кеші та дані ключів
     */
    public void clearAll() {
        groupKeyCache.clear();
        // TODO: Видалити ключі з KeyStore, якщо необхідно
    }
}