package com.lowlifedev.webhook.util;

import java.security.SecureRandom;
import java.util.Base64;
import org.springframework.stereotype.Component;

@Component
public class WebhookSecretGenerator {

    private static final String PREFIX = "whsec_";

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        byte[] randomBytes = new byte[32];

        secureRandom.nextBytes(randomBytes);

        String secret = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(randomBytes);

        return PREFIX + secret;
    }
}
