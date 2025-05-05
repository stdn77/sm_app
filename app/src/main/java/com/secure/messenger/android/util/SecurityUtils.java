package com.secure.messenger.android.util;

import android.content.Context;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.secure.messenger.android.data.local.PreferenceManager;

/**
 * Клас для безпечного зберігання ключів та криптографічних операцій
 */
public class SecurityUtils {
    private static final String TAG = "SecurityUtils";

    // Константи для Android KeyStore
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEYPAIR_ALIAS = "secure_messenger_keypair";
    private static final String KEY_ALIAS_RSA = "secure_messenger_rsa_key";
    private static final String KEY_ALIAS_EC = "secure_messenger_ec_key";
    private static final String SYMMETRIC_KEY_ALIAS = "secure_messenger_symmetric_key";

    // Константи для алгоритмів шифрування
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    private final Context context;
    private final KeyStore keyStore;
    private final Map<String, SecretKey> groupKeyCache;
    private final PreferenceManager preferenceManager;

    /**
     * Конструктор
     *
     * @param context контекст додатку
     */
    public SecurityUtils(Context context) {
        this.context = context;
        this.groupKeyCache = new HashMap<>();
        this.preferenceManager = new PreferenceManager(context);

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
            if (!preferenceManager.isKeysGenerated()) {
                generateRSAKeyPair();
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
     * Генерує та зберігає ключі для шифрування
     *
     * @throws Exception якщо виникла помилка під час генерації ключів
     */
    public void generateAndStoreKeys() throws Exception {
        generateRSAKeyPair();
        generateECKeyPair();
    }

    /**
     * Генерує пару ключів RSA для асиметричного шифрування
     *
     * @return пара ключів RSA
     * @throws Exception якщо виникла помилка
     */
    public KeyPair generateRSAKeyPair() throws Exception {
        if (keyStore == null) {
            throw new IllegalStateException("KeyStore not initialized");
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEYSTORE);

        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        KEY_ALIAS_RSA,
                        KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_RSA_PKCS1,
                                KeyProperties.ENCRYPTION_PADDING_RSA_OAEP)
                        .setKeySize(2048)
                        .build());

        return keyPairGenerator.generateKeyPair();
    }

    /**
     * Генерує пару ключів EC для цифрового підпису і обміну ключами
     *
     * @return пара ключів EC
     * @throws Exception якщо виникла помилка
     */
    public KeyPair generateECKeyPair() throws Exception {
        if (keyStore == null) {
            throw new IllegalStateException("KeyStore not initialized");
        }

        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_EC, ANDROID_KEYSTORE);

        keyPairGenerator.initialize(
                new KeyGenParameterSpec.Builder(
                        KEY_ALIAS_EC,
                        KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA512)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec("secp256r1"))
                        .build());

        return keyPairGenerator.generateKeyPair();
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

        return keyStore.getCertificate(KEY_ALIAS_RSA).getPublicKey();
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

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                KEY_ALIAS_RSA, null);

        return privateKeyEntry.getPrivateKey();
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
        return encryptWithRSA(data, publicKey);
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
        return decryptWithRSA(encryptedData, privateKey);
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
        return encryptWithAES(data, groupKey);
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
        return decryptWithAES(encryptedData, groupKey);
    }

    /**
     * Генерує ключ для нової групи
     *
     * @param groupId ідентифікатор групи
     * @return згенерований ключ групи
     * @throws Exception якщо виникла помилка
     */
    public SecretKey generateGroupKey(String groupId) throws Exception {
        SecretKey groupKey = generateAESKey();
        addGroupKey(groupId, groupKey);
        return groupKey;
    }

    /**
     * Отримує публічний ключ для відправки на сервер
     *
     * @return байтовий масив публічного ключа або null у разі помилки
     */
    public static byte[] getPublicKeyForServer() {
        try {
            KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
            keyStore.load(null);
            PublicKey publicKey = keyStore.getCertificate(KEY_ALIAS_RSA).getPublicKey();
            return publicKey.getEncoded();
        } catch (Exception e) {
            Log.e(TAG, "Error getting public key for server: " + e.getMessage(), e);
            return null;
        }
    }

    /**
     * Шифрує дані з використанням AES-GCM
     *
     * @param data дані для шифрування
     * @param secretKey секретний ключ AES
     * @return зашифровані дані з IV
     * @throws Exception якщо виникла помилка під час шифрування
     */
    public static byte[] encryptWithAES(byte[] data, SecretKey secretKey) throws Exception {
        // Генерація IV (Initialization Vector)
        byte[] iv = new byte[GCM_IV_LENGTH];
        new SecureRandom().nextBytes(iv);

        // Створення параметрів GCM
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        // Шифрування даних
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmParameterSpec);
        byte[] encryptedData = cipher.doFinal(data);

        // Комбінування IV та зашифрованих даних
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(iv);
        outputStream.write(encryptedData);
        return outputStream.toByteArray();
    }

    /**
     * Перетворює байтовий масив у публічний ключ
     *
     * @param keyBytes байтовий масив, що містить закодований публічний ключ
     * @return об'єкт PublicKey або null у разі помилки
     */
    public static PublicKey bytesToPublicKey(byte[] keyBytes) {
        try {
            java.security.spec.X509EncodedKeySpec keySpec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            java.security.KeyFactory keyFactory = java.security.KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (Exception e) {
            Log.e(TAG, "Error converting bytes to public key: " + e.getMessage(), e);
            return null;
        }
    }


    /**
     * Розшифровує дані з використанням AES-GCM
     *
     * @param encryptedData зашифровані дані з IV
     * @param secretKey секретний ключ AES
     * @return розшифровані дані
     * @throws Exception якщо виникла помилка під час розшифрування
     */
    public static byte[] decryptWithAES(byte[] encryptedData, SecretKey secretKey) throws Exception {
        // Виділення IV
        byte[] iv = Arrays.copyOfRange(encryptedData, 0, GCM_IV_LENGTH);
        byte[] encryptedContent = Arrays.copyOfRange(encryptedData, GCM_IV_LENGTH, encryptedData.length);

        // Створення параметрів GCM
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);

        // Розшифрування даних
        Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);
        return cipher.doFinal(encryptedContent);
    }

    /**
     * Шифрує дані з використанням RSA
     *
     * @param data дані для шифрування
     * @param publicKey публічний ключ RSA
     * @return зашифровані дані
     * @throws Exception якщо виникла помилка під час шифрування
     */
    public static byte[] encryptWithRSA(byte[] data, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }

    /**
     * Розшифровує дані з використанням RSA
     *
     * @param encryptedData зашифровані дані
     * @param privateKey приватний ключ RSA
     * @return розшифровані дані
     * @throws Exception якщо виникла помилка під час розшифрування
     */
    public static byte[] decryptWithRSA(byte[] encryptedData, PrivateKey privateKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(encryptedData);
    }

    /**
     * Генерує секретний ключ AES
     *
     * @return секретний ключ AES
     * @throws NoSuchAlgorithmException якщо алгоритм не підтримується
     */
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(256);
        return keyGenerator.generateKey();
    }

    /**
     * Створює секретний ключ AES з байтового масиву
     *
     * @param keyBytes байти ключа
     * @return секретний ключ AES
     */
    public static SecretKey createAESKeyFromBytes(byte[] keyBytes) {
        return new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * Генерує випадкову сіль
     *
     * @param length довжина солі в байтах
     * @return випадкова сіль
     */
    public static byte[] generateSalt(int length) {
        byte[] salt = new byte[length];
        new SecureRandom().nextBytes(salt);
        return salt;
    }

    /**
     * Обчислює хеш пароля з використанням SHA-256 і солі
     *
     * @param password пароль
     * @param salt сіль
     * @return хеш пароля
     * @throws NoSuchAlgorithmException якщо алгоритм не підтримується
     */
    public static byte[] hashPassword(String password, byte[] salt) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        md.update(salt);
        return md.digest(password.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Генерує випадковий ідентифікатор пристрою
     *
     * @return ідентифікатор пристрою
     */
    public static String generateDeviceId() {
        return UUID.randomUUID().toString();
    }

    /**
     * Кодує байти в Base64
     *
     * @param data байти для кодування
     * @return рядок у форматі Base64
     */
    public static String encodeBase64(byte[] data) {
        return Base64.encodeToString(data, Base64.NO_WRAP);
    }

    /**
     * Декодує рядок з Base64
     *
     * @param base64 рядок у форматі Base64
     * @return декодовані байти
     */
    public static byte[] decodeBase64(String base64) {
        return Base64.decode(base64, Base64.NO_WRAP);
    }

    /**
     * Зберігає зашифрований ключ групи
     *
     * @param groupId ідентифікатор групи
     * @param encryptedKey зашифрований ключ групи
     * @throws Exception якщо виникла помилка
     */
    public void saveEncryptedGroupKey(String groupId, byte[] encryptedKey) throws Exception {
        // Можна реалізувати зберігання в базі даних або SharedPreferences
    }

    /**
     * Очищає всі кеші та дані ключів
     */
    public void clearAll() {
        groupKeyCache.clear();
    }
}