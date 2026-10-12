package com.lowlifedev.webhook.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.Instant;
import tools.jackson.databind.JsonNode;

public record EventIngestRequest(
    @NotBlank(message = "type is required")
    @Size(max = 100, message = "type must be at most 100 characters")
    @Pattern(
        regexp = "^[a-z0-9]+(?:[._-][a-z0-9]+)*$",
        message = "type must be a valid event type"
    )
    String type,

    @Min(value = 1, message = "version must be at least 1") Integer version,

    @Size(max = 255, message = "orderingKey must be at most 255 characters")
    String orderingKey,

    @NotNull(message = "data is required") JsonNode data,

    JsonNode metadata,

    Instant occurredAt
) {}
