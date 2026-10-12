package com.lowlifedev.webhook.model;

public enum DeliveryStatus {
    PENDING,
    IN_PROGRESS,
    SUCCEEDED,
    RETRY_SCHEDULED,
    DEAD_LETTERED,
}
