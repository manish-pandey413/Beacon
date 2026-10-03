package com.lowlifedev.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record SubscriptionCreateRequest(
    @NotBlank(message = "eventType is required")
    @Size(max = 100, message = "eventType must be at most 100 characters")
    @Pattern(
        regexp = "^\\*|[a-z0-9]+(?:[._-][a-z0-9]+)*$",
        message = "eventType must be '*' or a valid event type"
    )
    String eventType
) {}
