package com.lowlifedev.webhook.controller;

import com.lowlifedev.webhook.dto.SubscriptionCreateRequest;
import com.lowlifedev.webhook.dto.SubscriptionResponse;
import com.lowlifedev.webhook.dto.WebhookEndpointCreateRequest;
import com.lowlifedev.webhook.dto.WebhookEndpointCreateResponse;
import com.lowlifedev.webhook.dto.WebhookEndpointResponse;
import com.lowlifedev.webhook.service.SubscriptionService;
import com.lowlifedev.webhook.service.WebhookEndpointService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tenants/{tenantId}/webhooks")
public class WebhookEndpointController {

    private final WebhookEndpointService endpointService;
    private final SubscriptionService subscriptionService;

    public WebhookEndpointController(
        WebhookEndpointService endpointService,
        SubscriptionService subscriptionService
    ) {
        this.endpointService = endpointService;
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<WebhookEndpointCreateResponse> create(
        @PathVariable UUID tenantId,
        @Valid @RequestBody WebhookEndpointCreateRequest request
    ) {
        WebhookEndpointCreateResponse response = endpointService.create(
            tenantId,
            request
        );

        URI location = URI.create(
            "/v1/tenants/" + tenantId + "/webhooks/" + response.id()
        );

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<WebhookEndpointResponse>> getAll(
        @PathVariable UUID tenantId
    ) {
        return ResponseEntity.ok(endpointService.getAll(tenantId));
    }

    @GetMapping("/{endpointId}")
    public ResponseEntity<WebhookEndpointResponse> getById(
        @PathVariable UUID tenantId,
        @PathVariable UUID endpointId
    ) {
        return ResponseEntity.ok(endpointService.getById(tenantId, endpointId));
    }

    @DeleteMapping("/{endpointId}")
    public ResponseEntity<Void> deactivate(
        @PathVariable UUID tenantId,
        @PathVariable UUID endpointId
    ) {
        endpointService.deactivate(tenantId, endpointId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{endpointId}/subscriptions")
    public ResponseEntity<SubscriptionResponse> createSubscription(
        @PathVariable UUID tenantId,
        @PathVariable UUID endpointId,
        @Valid @RequestBody SubscriptionCreateRequest request
    ) {
        SubscriptionResponse response = subscriptionService.create(
            tenantId,
            endpointId,
            request
        );

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{endpointId}/subscriptions")
    public ResponseEntity<List<SubscriptionResponse>> getSubscriptions(
        @PathVariable UUID tenantId,
        @PathVariable UUID endpointId
    ) {
        return ResponseEntity.ok(
            subscriptionService.getAll(tenantId, endpointId)
        );
    }

    @DeleteMapping("/{endpointId}/subscriptions/{subscriptionId}")
    public ResponseEntity<Void> deactivateSubscription(
        @PathVariable UUID tenantId,
        @PathVariable UUID endpointId,
        @PathVariable UUID subscriptionId
    ) {
        subscriptionService.deactivate(tenantId, endpointId, subscriptionId);

        return ResponseEntity.noContent().build();
    }
}
