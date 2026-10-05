package com.lowlifedev.webhook.exception;

public class SubscriptionAlreadyExistsException extends RuntimeException {

    public SubscriptionAlreadyExistsException(String eventType) {
        super("Subscription already exists for event type: " + eventType);
    }
}
