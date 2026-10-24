package com.lowlifedev.webhook.exception;

public class IdempotencyKeyReuseException extends RuntimeException {

    public IdempotencyKeyReuseException() {
        super("This Idempotency-Key was already used with a different request");
    }
}
