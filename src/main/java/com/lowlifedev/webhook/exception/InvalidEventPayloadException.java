package com.lowlifedev.webhook.exception;

public class InvalidEventPayloadException extends RuntimeException {

    public InvalidEventPayloadException(String message) {
        super(message);
    }
}
