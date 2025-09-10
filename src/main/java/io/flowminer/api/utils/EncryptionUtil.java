package io.flowminer.api.utils;

import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;

public class EncryptionUtil {
    private static final String PASSWORD = System.getenv("ENCRYPTION_KEY");
    private static final String SALT = System.getenv("ENCRYPTION_SALT");

    private static final TextEncryptor textEncryptor = Encryptors.text(PASSWORD, SALT);

    public static String encrypt(String rawValue) {
        return textEncryptor.encrypt(rawValue);
    }
    public static String decrypt(String encryptedValue) {
        return textEncryptor.decrypt(encryptedValue);
    }
}
