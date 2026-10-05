package com.lowlifedev.webhook.repository;

import com.lowlifedev.webhook.model.WebhookEndpoint;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WebhookEndpointRepository
    extends JpaRepository<WebhookEndpoint, UUID>
{
    List<WebhookEndpoint> findAllByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    Optional<WebhookEndpoint> findByTenantIdAndId(UUID tenantId, UUID id);
}
