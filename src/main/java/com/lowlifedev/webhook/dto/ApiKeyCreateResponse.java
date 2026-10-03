package com.lowlifedev.webhook.dto;

import java.time.Instant;
import java.util.UUID;

public record ApiKeyCreateResponse(
    UUID id,
    UUID tenantId,
    String name,
    String keyPrefix,
    String secret,
    String status,
    Instant createdAt,
    Instant expiresAt
) {}
