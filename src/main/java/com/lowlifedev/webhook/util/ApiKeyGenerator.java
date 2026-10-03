package com.lowlifedev.webhook.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyGenerator {

    private static final String PREFIX = "wk_live_";

    private final SecureRandom secureRandom = new SecureRandom();

    public GeneratedApiKey generate() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String secretPart = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        String rawKey = PREFIX + secretPart;

        String keyPrefix = rawKey.substring(0, Math.min(16, rawKey.length()));

        String hash = sha256(rawKey);

        return new GeneratedApiKey(rawKey, keyPrefix, hash);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                value.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

            StringBuilder hex = new StringBuilder(hash.length * 2);

            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is unavailable",
                exception
            );
        }
    }

    public record GeneratedApiKey(
        String rawKey,
        String keyPrefix,
        String hash
    ) {}
}
