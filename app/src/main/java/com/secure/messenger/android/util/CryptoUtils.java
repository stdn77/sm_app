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
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Утилітний клас для криптографічних операцій
 */
public class CryptoUtils {
    private static final String TAG = "CryptoUtils";

    // Константи для Android KeyStore
    private static final String ANDROID_KEYSTORE = "AndroidKeyStore";
    private static final String KEY_ALIAS_RSA = "secure_messenger_rsa_key";
    private static final String KEY_ALIAS_EC = "secure_messenger_ec_key";

    // Константи для алгоритмів шифрування
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    private static final int GCM_IV_LENGTH = 12;

    /**
     * Генерує та зберігає ключі для шифрування в Android KeyStore
     *
     * @param context контекст додатку
     * @throws Exception якщо виникла помилка під час генерації ключів
     */
    public static void generateAndStoreKeys(Context context) throws Exception {
        generateRSAKeyPair();
        generateECKeyPair();
    }

    /**
     * Генерує пару ключів RSA для асиметричного шифрування
     *
     * @return пара ключів RSA
     * @throws NoSuchAlgorithmException якщо алгоритм не підтримується
     * @throws NoSuchProviderException якщо провайдер не знайдено
     * @throws InvalidAlgorithmParameterException якщо параметри алгоритму невірні
     */
    public static KeyPair generateRSAKeyPair() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {

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
     * @throws NoSuchAlgorithmException якщо алгоритм не підтримується
     * @throws NoSuchProviderException якщо провайдер не знайдено
     * @throws InvalidAlgorithmParameterException якщо параметри алгоритму невірні
     */
    public static KeyPair generateECKeyPair() throws NoSuchAlgorithmException,
            NoSuchProviderException, InvalidAlgorithmParameterException {

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
     * Отримує публічний ключ RSA з KeyStore
     *
     * @return публічний ключ RSA
     * @throws Exception якщо виникла помилка під час отримання ключа
     */
    public static PublicKey getRSAPublicKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        return keyStore.getCertificate(KEY_ALIAS_RSA).getPublicKey();
    }

    /**
     * Отримує приватний ключ RSA з KeyStore
     *
     * @return приватний ключ RSA
     * @throws Exception якщо виникла помилка під час отримання ключа
     */
    public static PrivateKey getRSAPrivateKey() throws Exception {
        KeyStore keyStore = KeyStore.getInstance(ANDROID_KEYSTORE);
        keyStore.load(null);

        KeyStore.PrivateKeyEntry privateKeyEntry = (KeyStore.PrivateKeyEntry) keyStore.getEntry(
                KEY_ALIAS_RSA, null);

        return privateKeyEntry.getPrivateKey();
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
}