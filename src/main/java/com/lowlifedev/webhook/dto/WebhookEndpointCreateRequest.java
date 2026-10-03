package com.lowlifedev.webhook.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record WebhookEndpointCreateRequest(
    @NotBlank(message = "name is required")
    @Size(max = 100, message = "name must be at most 100 characters")
    String name,

    @NotBlank(message = "url is required")
    @Size(max = 2048, message = "url must be at most 2048 characters")
    String url,

    @Min(value = 100, message = "timeoutMs must be at least 100")
    @Max(value = 120000, message = "timeoutMs must be at most 120000")
    Integer timeoutMs,

    @Min(value = 1, message = "maxAttempts must be at least 1")
    @Max(value = 20, message = "maxAttempts must be at most 20")
    Integer maxAttempts,

    @Min(value = 0, message = "retryBaseDelayMs must not be negative")
    Long retryBaseDelayMs,

    @Min(value = 0, message = "retryMaxDelayMs must not be negative")
    Long retryMaxDelayMs
) {}
