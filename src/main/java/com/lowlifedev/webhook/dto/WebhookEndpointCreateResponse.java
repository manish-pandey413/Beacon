package com.lowlifedev.webhook.dto;

import java.time.Instant;
import java.util.UUID;

public record WebhookEndpointCreateResponse(
    UUID id,
    UUID tenantId,
    String name,
    String url,
    String secret,
    String status,
    int secretVersion,
    int timeoutMs,
    int maxAttempts,
    long retryBaseDelayMs,
    long retryMaxDelayMs,
    Instant createdAt
) {}

