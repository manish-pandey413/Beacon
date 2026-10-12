package com.lowlifedev.webhook.repository;

import com.lowlifedev.webhook.model.Subscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository
    extends JpaRepository<Subscription, UUID>
{
    List<Subscription> findAllByEndpointIdOrderByCreatedAtAsc(UUID endpointId);

    boolean existsByEndpointIdAndEventType(UUID endpointId, String eventType);

    Optional<Subscription> findByIdAndEndpointId(UUID id, UUID endpointId);

    @Query(
        value = """
        SELECT DISTINCT e.id
        FROM subscriptions s
        JOIN webhook_endpoints e
          ON e.id = s.endpoint_id
        WHERE e.tenant_id = :tenantId
          AND e.status = 'ACTIVE'
          AND s.active = TRUE
          AND (
              s.event_type = :eventType
              OR s.event_type = '*'
          )
        ORDER BY e.id
        """,
        nativeQuery = true
    )
    List<UUID> findMatchingActiveEndpointIds(
        @Param("tenantId") UUID tenantId,
        @Param("eventType") String eventType
    );
}
