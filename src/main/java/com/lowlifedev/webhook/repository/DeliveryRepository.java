package com.lowlifedev.webhook.repository;

import com.lowlifedev.webhook.model.Delivery;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    long countByTenantIdAndEventId(UUID tenantId, UUID eventId);
}
