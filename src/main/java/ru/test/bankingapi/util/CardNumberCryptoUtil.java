package ru.test.bankingapi.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Утилита шифрования и HMAC-хэширования номера карты.
 */
@Component
public class CardNumberCryptoUtil {
    private static final String AES_GCM = "AES/GCM/NoPadding";
    private static final String HMAC_SHA_256 = "HmacSHA256";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecureRandom secureRandom = new SecureRandom();
    private final SecretKeySpec encryptionKeySpec;
    private final SecretKeySpec hashKeySpec;

    public CardNumberCryptoUtil(
            @Value("${card.crypto.key}") String base64EncryptionKey,
            @Value("${card.hash.key}") String base64HashKey
    ) {
        byte[] encryptionKey = decodeBase64(base64EncryptionKey, "Некорректный ключ шифрования карты");
        if (encryptionKey.length != 16 && encryptionKey.length != 24 && encryptionKey.length != 32) {
            throw new IllegalArgumentException("Ключ шифрования карты должен быть 128, 192 или 256 бит");
        }

        byte[] hashKey = decodeBase64(base64HashKey, "Некорректный ключ HMAC для номера карты");
        if (hashKey.length == 0) {
            throw new IllegalArgumentException("Ключ HMAC для номера карты не должен быть пустым");
        }

        this.encryptionKeySpec = new SecretKeySpec(encryptionKey, "AES");
        this.hashKeySpec = new SecretKeySpec(hashKey, HMAC_SHA_256);
    }

    public String encrypt(String plainNumber) {
        validateCardNumber(plainNumber);

        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKeySpec, new GCMParameterSpec(TAG_LENGTH, iv));

            byte[] encrypted = cipher.doFinal(plainNumber.getBytes(StandardCharsets.UTF_8));
            ByteBuffer buffer = ByteBuffer.allocate(iv.length + encrypted.length);
            buffer.put(iv);
            buffer.put(encrypted);
            return Base64.getEncoder().encodeToString(buffer.array());
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось зашифровать номер карты", ex);
        }
    }

    public String decrypt(String encryptedNumber) {
        try {
            byte[] payload = decodeBase64(encryptedNumber, "Некорректные зашифрованные данные карты");
            if (payload.length <= IV_LENGTH) {
                throw new IllegalArgumentException("Некорректные зашифрованные данные карты");
            }

            ByteBuffer buffer = ByteBuffer.wrap(payload);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] encrypted = new byte[buffer.remaining()];
            buffer.get(encrypted);

            Cipher cipher = Cipher.getInstance(AES_GCM);
            cipher.init(Cipher.DECRYPT_MODE, encryptionKeySpec, new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось расшифровать номер карты", ex);
        }
    }

    public String hash(String plainNumber) {
        validateCardNumber(plainNumber);

        try {
            Mac mac = Mac.getInstance(HMAC_SHA_256);
            mac.init(hashKeySpec);
            return HexFormat.of().formatHex(mac.doFinal(plainNumber.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Не удалось вычислить HMAC номера карты", ex);
        }
    }

    private byte[] decodeBase64(String value, String errorMessage) {
        try {
            return Base64.getDecoder().decode(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException(errorMessage, ex);
        }
    }

    private void validateCardNumber(String plainNumber) {
        if (plainNumber == null || !plainNumber.matches("\\d{16}")) {
            throw new IllegalArgumentException("Номер карты должен содержать ровно 16 цифр");
        }
    }
}