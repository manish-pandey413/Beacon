package com.lowlifedev.webhook.service;

import com.lowlifedev.webhook.dto.EventIngestRequest;
import com.lowlifedev.webhook.dto.EventIngestResponse;
import com.lowlifedev.webhook.exception.IdempotencyKeyReuseException;
import com.lowlifedev.webhook.exception.InvalidEventPayloadException;
import com.lowlifedev.webhook.exception.InvalidIdempotencyKeyException;
import com.lowlifedev.webhook.messaging.DeliveryRequestedMessage;
import com.lowlifedev.webhook.model.Delivery;
import com.lowlifedev.webhook.model.Event;
import com.lowlifedev.webhook.model.IdempotencyRecord;
import com.lowlifedev.webhook.model.OutboxMessage;
import com.lowlifedev.webhook.repository.DeliveryRepository;
import com.lowlifedev.webhook.repository.EventRepository;
import com.lowlifedev.webhook.repository.IdempotencyRecordRepository;
import com.lowlifedev.webhook.repository.OutboxMessageRepository;
import com.lowlifedev.webhook.repository.SubscriptionRepository;
import com.lowlifedev.webhook.security.EventRequestHasher;
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
    private final IdempotencyRecordRepository idempotencyRepository;
    private final EventRequestHasher requestHasher;
    private final ObjectMapper objectMapper;

    private static final String DELIVERY_REQUESTS_TOPIC = "delivery-requests";

    private final OutboxMessageRepository outboxRepository;

    public EventService(
        EventRepository eventRepository,
        DeliveryRepository deliveryRepository,
        SubscriptionRepository subscriptionRepository,
        IdempotencyRecordRepository idempotencyRepository,
        EventRequestHasher requestHasher,
        ObjectMapper objectMapper,
        OutboxMessageRepository outboxRepository
    ) {
        this.eventRepository = eventRepository;
        this.deliveryRepository = deliveryRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.idempotencyRepository = idempotencyRepository;
        this.requestHasher = requestHasher;
        this.objectMapper = objectMapper;
        this.outboxRepository = outboxRepository;
    }

    @Transactional
    public EventIngestResponse ingest(
        UUID tenantId,
        String suppliedIdempotencyKey,
        EventIngestRequest request
    ) {
        validatePayload(request.data(), request.metadata());

        String idempotencyKey = validateIdempotencyKey(suppliedIdempotencyKey);

        String requestHash = requestHasher.hash(request);

        idempotencyRepository.deleteExpiredForKey(tenantId, idempotencyKey);

        int claimed = idempotencyRepository.tryCreate(
            UUID.randomUUID(),
            tenantId,
            idempotencyKey,
            requestHash
        );

        if (claimed == 0) {
            return returnExistingResult(tenantId, idempotencyKey, requestHash);
        }

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

        Event savedEvent = eventRepository.saveAndFlush(event);

        List<UUID> endpointIds =
            subscriptionRepository.findMatchingActiveEndpointIds(
                tenantId,
                request.type()
            );

        List<Delivery> deliveries = endpointIds
            .stream()
            .map(endpointId ->
                new Delivery(tenantId, savedEvent.getId(), endpointId)
            )
            .toList();

        List<Delivery> savedDeliveries = deliveries.isEmpty()
            ? List.of()
            : deliveryRepository.saveAllAndFlush(deliveries);

        List<OutboxMessage> outboxMessages = savedDeliveries
            .stream()
            .map(delivery -> {
                UUID messageId = UUID.randomUUID();

                DeliveryRequestedMessage payload = new DeliveryRequestedMessage(
                    messageId,
                    "delivery.requested",
                    1,
                    tenantId,
                    savedEvent.getId(),
                    delivery.getId(),
                    delivery.getEndpointId()
                );

                JsonNode payloadJson = objectMapper.valueToTree(payload);

                String messageKey = createMessageKey(
                    tenantId,
                    delivery.getEndpointId(),
                    savedEvent.getOrderingKey(),
                    delivery.getId()
                );

                return new OutboxMessage(
                    messageId,
                    payload.type(),
                    DELIVERY_REQUESTS_TOPIC,
                    messageKey,
                    delivery.getId(),
                    payloadJson
                );
            })
            .toList();

        if (!outboxMessages.isEmpty()) {
            outboxRepository.saveAllAndFlush(outboxMessages);
        }

        int attached = idempotencyRepository.attachEvent(
            tenantId,
            idempotencyKey,
            savedEvent.getId()
        );

        if (attached != 1) {
            throw new IllegalStateException(
                "Unable to associate idempotency key with event"
            );
        }

        return EventIngestResponse.accepted(savedEvent, savedDeliveries.size());
    }

    private EventIngestResponse returnExistingResult(
        UUID tenantId,
        String idempotencyKey,
        String requestHash
    ) {
        IdempotencyRecord existing = idempotencyRepository
            .findByTenantIdAndIdempotencyKey(tenantId, idempotencyKey)
            .orElseThrow(() ->
                new IllegalStateException(
                    "Conflicting idempotency record was not found"
                )
            );

        if (!existing.getRequestHash().equals(requestHash)) {
            throw new IdempotencyKeyReuseException();
        }

        if (existing.getEventId() == null) {
            throw new IllegalStateException(
                "Idempotency record has no associated event"
            );
        }

        Event existingEvent = eventRepository
            .findByIdAndTenantId(existing.getEventId(), tenantId)
            .orElseThrow(() ->
                new IllegalStateException(
                    "Idempotency record references a missing event"
                )
            );

        long deliveryCount = deliveryRepository.countByTenantIdAndEventId(
            tenantId,
            existingEvent.getId()
        );

        return EventIngestResponse.accepted(
            existingEvent,
            Math.toIntExact(deliveryCount)
        );
    }

    private String validateIdempotencyKey(String suppliedKey) {
        if (suppliedKey == null) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key header is required"
            );
        }

        String key = suppliedKey.trim();

        if (key.isEmpty()) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key must not be blank"
            );
        }

        if (key.length() > 255) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key must be at most 255 characters"
            );
        }

        if (key.chars().anyMatch(Character::isISOControl)) {
            throw new InvalidIdempotencyKeyException(
                "Idempotency-Key contains invalid control characters"
            );
        }

        return key;
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

    private String createMessageKey(
        UUID tenantId,
        UUID endpointId,
        String orderingKey,
        UUID deliveryId
    ) {
        if (orderingKey == null || orderingKey.isBlank()) {
            return "delivery:" + deliveryId;
        }

        return tenantId + ":" + endpointId + ":" + orderingKey;
    }
}
