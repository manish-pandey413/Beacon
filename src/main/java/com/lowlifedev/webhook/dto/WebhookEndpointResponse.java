package com.lowlifedev.webhook.dto;

import com.lowlifedev.webhook.model.WebhookEndpoint;
import java.time.Instant;
import java.util.UUID;

public record WebhookEndpointResponse(
    UUID id,
    UUID tenantId,
    String name,
    String url,
    String status,
    int secretVersion,
    int timeoutMs,
    int maxAttempts,
    long retryBaseDelayMs,
    long retryMaxDelayMs,
    Instant createdAt,
    Instant updatedAt
) {
    public static WebhookEndpointResponse from(WebhookEndpoint endpoint) {
        return new WebhookEndpointResponse(
            endpoint.getId(),
            endpoint.getTenantId(),
            endpoint.getName(),
            endpoint.getUrl(),
            endpoint.getStatus().name(),
            endpoint.getSecretVersion(),
            endpoint.getTimeoutMs(),
            endpoint.getMaxAttempts(),
            endpoint.getRetryBaseDelayMs(),
            endpoint.getRetryMaxDelayMs(),
            endpoint.getCreatedAt(),
            endpoint.getUpdatedAt()
        );
    }
}
