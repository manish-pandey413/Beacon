package com.lowlifedev.webhook.dto;

import com.lowlifedev.webhook.model.Event;
import java.time.Instant;
import java.util.UUID;

public record EventIngestResponse(
    UUID eventId,
    String type,
    int version,
    String status,
    int deliveriesCreated,
    Instant acceptedAt
) {
    public static EventIngestResponse accepted(
        Event event,
        int deliveriesCreated
    ) {
        return new EventIngestResponse(
            event.getId(),
            event.getEventType(),
            event.getVersion(),
            "ACCEPTED",
            deliveriesCreated,
            event.getCreatedAt()
        );
    }
}
