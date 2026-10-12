package com.lowlifedev.webhook.exception;

public class InvalidApiKeyException extends RuntimeException {

    public InvalidApiKeyException() {
        super("Invalid or inactive API key");
    }
}
