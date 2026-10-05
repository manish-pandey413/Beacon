package com.lowlifedev.webhook.service;

import com.lowlifedev.webhook.dto.SubscriptionCreateRequest;
import com.lowlifedev.webhook.dto.SubscriptionResponse;
import com.lowlifedev.webhook.exception.SubscriptionAlreadyExistsException;
import com.lowlifedev.webhook.exception.SubscriptionNotFoundException;
import com.lowlifedev.webhook.model.Subscription;
import com.lowlifedev.webhook.repository.SubscriptionRepository;
import com.lowlifedev.webhook.repository.WebhookEndpointRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final WebhookEndpointRepository endpointRepository;

    public SubscriptionService(
        SubscriptionRepository subscriptionRepository,
        WebhookEndpointRepository endpointRepository
    ) {
        this.subscriptionRepository = subscriptionRepository;
        this.endpointRepository = endpointRepository;
    }

    @Transactional
    public SubscriptionResponse create(
        UUID tenantId,
        UUID endpointId,
        SubscriptionCreateRequest request
    ) {
        ensureEndpointBelongsToTenant(tenantId, endpointId);

        if (
            subscriptionRepository.existsByEndpointIdAndEventType(
                endpointId,
                request.eventType()
            )
        ) {
            throw new SubscriptionAlreadyExistsException(request.eventType());
        }

        Subscription subscription = new Subscription(
            endpointId,
            request.eventType()
        );

        try {
            Subscription saved = subscriptionRepository.save(subscription);

            return SubscriptionResponse.from(saved);
        } catch (DataIntegrityViolationException exception) {
            /*
             * The UNIQUE constraint is still the final
             * concurrency safeguard.
             */
            throw new SubscriptionAlreadyExistsException(request.eventType());
        }
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> getAll(UUID tenantId, UUID endpointId) {
        ensureEndpointBelongsToTenant(tenantId, endpointId);

        return subscriptionRepository
            .findAllByEndpointIdOrderByCreatedAtAsc(endpointId)
            .stream()
            .map(SubscriptionResponse::from)
            .toList();
    }

    @Transactional
    public void deactivate(
        UUID tenantId,
        UUID endpointId,
        UUID subscriptionId
    ) {
        ensureEndpointBelongsToTenant(tenantId, endpointId);

        Subscription subscription = subscriptionRepository
            .findByIdAndEndpointId(subscriptionId, endpointId)
            .orElseThrow(() ->
                new SubscriptionNotFoundException(subscriptionId)
            );

        subscription.deactivate();
    }

    private void ensureEndpointBelongsToTenant(UUID tenantId, UUID endpointId) {
        endpointRepository
            .findByTenantIdAndId(tenantId, endpointId)
            .orElseThrow(() ->
                new com.lowlifedev.webhook.exception.WebhookEndpointNotFoundException(
                    endpointId
                )
            );
    }
}
