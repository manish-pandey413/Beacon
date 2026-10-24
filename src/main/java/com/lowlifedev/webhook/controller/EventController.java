package com.lowlifedev.webhook.controller;

import com.lowlifedev.webhook.dto.EventIngestRequest;
import com.lowlifedev.webhook.dto.EventIngestResponse;
import com.lowlifedev.webhook.service.ApiKeyAuthenticationService;
import com.lowlifedev.webhook.service.EventService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/events")
public class EventController {

    private final ApiKeyAuthenticationService authenticationService;
    private final EventService eventService;

    public EventController(
        ApiKeyAuthenticationService authenticationService,
        EventService eventService
    ) {
        this.authenticationService = authenticationService;
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<EventIngestResponse> ingest(
        @RequestHeader(
            name = "Authorization",
            required = false
        ) String authorizationHeader,
        @RequestHeader(
            name = "Idempotency-Key",
            required = false
        ) String idempotencyKey,
        @Valid @RequestBody EventIngestRequest request
    ) {
        var tenantId = authenticationService.authenticate(authorizationHeader);

        EventIngestResponse response = eventService.ingest(
            tenantId,
            idempotencyKey,
            request
        );

        return ResponseEntity.accepted().body(response);
    }
}
