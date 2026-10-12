package com.lowlifedev.webhook.service;

import com.lowlifedev.webhook.dto.EventIngestRequest;
import com.lowlifedev.webhook.dto.EventIngestResponse;
import com.lowlifedev.webhook.exception.InvalidEventPayloadException;
import com.lowlifedev.webhook.model.Delivery;
import com.lowlifedev.webhook.model.Event;
import com.lowlifedev.webhook.repository.DeliveryRepository;
import com.lowlifedev.webhook.repository.EventRepository;
import com.lowlifedev.webhook.repository.SubscriptionRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final DeliveryRepository deliveryRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ObjectMapper objectMapper;

    public EventService(
        EventRepository eventRepository,
        DeliveryRepository deliveryRepository,
        SubscriptionRepository subscriptionRepository,
        ObjectMapper objectMapper
    ) {
        this.eventRepository = eventRepository;
        this.deliveryRepository = deliveryRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public EventIngestResponse ingest(
        UUID tenantId,
        EventIngestRequest request
    ) {
        validatePayload(request.data(), request.metadata());

        int version = request.version() == null ? 1 : request.version();

        JsonNode metadata =
            request.metadata() == null
                ? objectMapper.createObjectNode()
                : request.metadata();

        Event event = new Event(
            tenantId,
            request.type(),
            version,
            request.orderingKey(),
            request.data(),
            metadata,
            request.occurredAt()
        );

        eventRepository.save(event);

        // Find matching active endpoints for this tenant.
        List<UUID> endpointIds =
            subscriptionRepository.findMatchingActiveEndpointIds(
                tenantId,
                request.type()
            );

        // Create one delivery per matching endpoint.
        List<Delivery> deliveries = endpointIds
            .stream()
            .map(endpointId ->
                new Delivery(tenantId, event.getId(), endpointId)
            )
            .toList();

        if (!deliveries.isEmpty()) {
            deliveryRepository.saveAll(deliveries);
        }

        return EventIngestResponse.accepted(event, deliveries.size());
    }

    private void validatePayload(JsonNode data, JsonNode metadata) {
        if (data == null || !data.isObject()) {
            throw new InvalidEventPayloadException(
                "data must be a JSON object"
            );
        }

        if (metadata != null && !metadata.isObject()) {
            throw new InvalidEventPayloadException(
                "metadata must be a JSON object"
            );
        }
    }
}
