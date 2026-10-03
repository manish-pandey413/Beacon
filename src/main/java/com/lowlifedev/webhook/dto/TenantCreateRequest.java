package com.lowlifedev.webhook.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TenantCreateRequest(

        @NotBlank(message = "name is required")
        @Size(max = 100, message = "name must be at most 100 characters")
        String name,

        @NotBlank(message = "slug is required")
        @Size(max = 100, message = "slug must be at most 100 characters")
        @Pattern(
                regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$",
                message = "slug must contain lowercase letters, numbers, and hyphens only"
        )
        String slug
) {
}
