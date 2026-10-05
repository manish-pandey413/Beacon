package com.lowlifedev.webhook.repository;

import com.lowlifedev.webhook.model.Subscription;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository
    extends JpaRepository<Subscription, UUID>
{
    List<Subscription> findAllByEndpointIdOrderByCreatedAtAsc(UUID endpointId);

    boolean existsByEndpointIdAndEventType(UUID endpointId, String eventType);

    Optional<Subscription> findByIdAndEndpointId(UUID id, UUID endpointId);
}
