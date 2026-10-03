package com.lowlifedev.webhook.dto;

import com.lowlifedev.webhook.model.ApiKey;
import java.time.Instant;
import java.util.UUID;

public record ApiKeyResponse(
    UUID id,
    UUID tenantId,
    String name,
    String keyPrefix,
    String status,
    Instant createdAt,
    Instant revokedAt,
    Instant expiresAt,
    Instant lastUsedAt
) {
    public static ApiKeyResponse from(ApiKey apiKey) {
        return new ApiKeyResponse(
            apiKey.getId(),
            apiKey.getTenantId(),
            apiKey.getName(),
            apiKey.getKeyPrefix(),
            apiKey.getStatus().name(),
            apiKey.getCreatedAt(),
            apiKey.getRevokedAt(),
            apiKey.getExpiresAt(),
            apiKey.getLastUsedAt()
        );
    }
}
