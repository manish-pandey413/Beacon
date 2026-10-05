package com.lowlifedev.webhook.dto;

import com.lowlifedev.webhook.model.Subscription;
import java.time.Instant;
import java.util.UUID;

public record SubscriptionResponse(
    UUID id,
    UUID endpointId,
    String eventType,
    boolean active,
    Instant createdAt
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
            subscription.getId(),
            subscription.getEndpointId(),
            subscription.getEventType(),
            subscription.isActive(),
            subscription.getCreatedAt()
        );
    }
}
