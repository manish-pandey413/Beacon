package com.lowlifedev.webhook.security;

import jakarta.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class WebhookSecretCrypto {

    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH_BITS = 128;

    private final String encodedMasterKey;

    private final SecureRandom secureRandom = new SecureRandom();

    private SecretKey masterKey;

    public WebhookSecretCrypto(
        @Value("${security.webhook.encryption-key}") String encodedMasterKey
    ) {
        this.encodedMasterKey = encodedMasterKey;
    }

    @PostConstruct
    void initialize() {
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(encodedMasterKey);
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                "Webhook encryption key must be valid Base64",
                exception
            );
        }

        if (keyBytes.length != 32) {
            throw new IllegalStateException(
                "Webhook encryption key must decode to 32 bytes"
            );
        }

        this.masterKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String plaintext) {
        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.ENCRYPT_MODE, masterKey, spec);

            byte[] ciphertext = cipher.doFinal(
                plaintext.getBytes(StandardCharsets.UTF_8)
            );

            return (
                Base64.getEncoder().encodeToString(iv) +
                ":" +
                Base64.getEncoder().encodeToString(ciphertext)
            );
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException(
                "Unable to encrypt webhook secret",
                exception
            );
        }
    }

    public String decrypt(String encryptedValue) {
        try {
            String[] parts = encryptedValue.split(":", 2);

            if (parts.length != 2) {
                throw new IllegalArgumentException(
                    "Invalid encrypted webhook secret"
                );
            }

            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] ciphertext = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance(ALGORITHM);

            GCMParameterSpec spec = new GCMParameterSpec(TAG_LENGTH_BITS, iv);

            cipher.init(Cipher.DECRYPT_MODE, masterKey, spec);

            byte[] plaintext = cipher.doFinal(ciphertext);

            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (
            GeneralSecurityException
            | IllegalArgumentException exception
        ) {
            throw new IllegalStateException(
                "Unable to decrypt webhook secret",
                exception
            );
        }
    }
}
