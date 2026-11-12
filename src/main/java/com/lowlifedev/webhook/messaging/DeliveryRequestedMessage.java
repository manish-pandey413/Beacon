package com.lowlifedev.webhook.messaging;

import java.util.UUID;

public record DeliveryRequestedMessage(
    UUID messageId,
    String type,
    int schemaVersion,
    UUID tenantId,
    UUID eventId,
    UUID deliveryId,
    UUID endpointId
) {}
