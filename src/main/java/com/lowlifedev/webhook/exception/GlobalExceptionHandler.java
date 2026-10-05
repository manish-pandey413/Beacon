package com.lowlifedev.webhook.exception;

import com.lowlifedev.webhook.dto.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TenantNotFoundException.class)
    public ResponseEntity<ApiError> handleTenantNotFound(
        TenantNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiError(
                "TENANT_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
            )
        );
    }

    @ExceptionHandler(TenantAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleTenantAlreadyExists(
        TenantAlreadyExistsException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ApiError(
                "TENANT_ALREADY_EXISTS",
                exception.getMessage(),
                Instant.now()
            )
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(
        MethodArgumentNotValidException exception
    ) {
        String message = exception
            .getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.joining(", "));

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError("VALIDATION_ERROR", message, Instant.now())
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleUnexpectedException(
        Exception exception,
        HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            new ApiError(
                "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred",
                Instant.now()
            )
        );
    }

    @ExceptionHandler(ApiKeyNotFoundException.class)
    public ResponseEntity<ApiError> handleApiKeyNotFound(
        ApiKeyNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiError(
                "API_KEY_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
            )
        );
    }

    @ExceptionHandler(WebhookEndpointNotFoundException.class)
    public ResponseEntity<ApiError> handleWebhookEndpointNotFound(
        WebhookEndpointNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiError(
                "WEBHOOK_ENDPOINT_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
            )
        );
    }

    @ExceptionHandler(SubscriptionNotFoundException.class)
    public ResponseEntity<ApiError> handleSubscriptionNotFound(
        SubscriptionNotFoundException exception
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            new ApiError(
                "SUBSCRIPTION_NOT_FOUND",
                exception.getMessage(),
                Instant.now()
            )
        );
    }

    @ExceptionHandler(SubscriptionAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleSubscriptionAlreadyExists(
        SubscriptionAlreadyExistsException exception
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            new ApiError(
                "SUBSCRIPTION_ALREADY_EXISTS",
                exception.getMessage(),
                Instant.now()
            )
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(
        IllegalArgumentException exception
    ) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            new ApiError(
                "INVALID_REQUEST",
                exception.getMessage(),
                Instant.now()
            )
        );
    }
}
