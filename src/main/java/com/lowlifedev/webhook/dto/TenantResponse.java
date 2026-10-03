package com.lowlifedev.webhook.dto;

import java.time.Instant;
import java.util.UUID;

import com.lowlifedev.webhook.model.Tenant;

public record TenantResponse(
        UUID id,
        String name,
        String slug,
        String status,
        Instant createdAt,
        Instant updatedAt
) {

    public static TenantResponse from(Tenant tenant) {
        return new TenantResponse(
                tenant.getId(),
                tenant.getName(),
                tenant.getSlug(),
                tenant.getStatus().name(),
                tenant.getCreatedAt(),
                tenant.getUpdatedAt()
        );
    }
}
