package com.lowlifedev.webhook.util;

import com.lowlifedev.webhook.security.ApiKeyHasher;
import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class ApiKeyGenerator {

    private static final String PREFIX = "wk_live_";

    private final SecureRandom secureRandom = new SecureRandom();

    private final ApiKeyHasher apiKeyHasher;

    public ApiKeyGenerator(ApiKeyHasher apiKeyHasher) {
        this.apiKeyHasher = apiKeyHasher;
    }

    public GeneratedApiKey generate() {
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);

        String secretPart = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        String rawKey = PREFIX + secretPart;

        String keyPrefix = rawKey.substring(0, Math.min(16, rawKey.length()));

        String hash = apiKeyHasher.hash(rawKey);

        return new GeneratedApiKey(rawKey, keyPrefix, hash);
    }

    public record GeneratedApiKey(
        String rawKey,
        String keyPrefix,
        String hash
    ) {}
}
