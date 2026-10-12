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
        @Valid @RequestBody EventIngestRequest request
    ) {
        // Identify the tenant from the credential, not the body.
        var tenantId = authenticationService.authenticate(authorizationHeader);

        // Persist the event and its matching deliveries.
        EventIngestResponse response = eventService.ingest(tenantId, request);

        return ResponseEntity.accepted().body(response);
    }
}
