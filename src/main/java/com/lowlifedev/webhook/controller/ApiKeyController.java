package com.lowlifedev.webhook.controller;

import com.lowlifedev.webhook.dto.ApiKeyCreateRequest;
import com.lowlifedev.webhook.dto.ApiKeyCreateResponse;
import com.lowlifedev.webhook.dto.ApiKeyResponse;
import com.lowlifedev.webhook.service.ApiKeyService;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/v1/tenants/{tenantId}/api-keys")
public class ApiKeyController {

    private final ApiKeyService apiKeyService;

    public ApiKeyController(ApiKeyService apiKeyService) {
        this.apiKeyService = apiKeyService;
    }

    @PostMapping
    public ResponseEntity<ApiKeyCreateResponse> create(
        @PathVariable UUID tenantId,
        @Valid @RequestBody ApiKeyCreateRequest request
    ) {
        ApiKeyCreateResponse response = apiKeyService.create(tenantId, request);

        URI location = URI.create(
            "/v1/tenants/" + tenantId + "/api-keys/" + response.id()
        );

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ApiKeyResponse>> getAll(
        @PathVariable UUID tenantId
    ) {
        return ResponseEntity.ok(apiKeyService.getAll(tenantId));
    }

    @DeleteMapping("/{apiKeyId}")
    public ResponseEntity<Void> revoke(
        @PathVariable UUID tenantId,
        @PathVariable UUID apiKeyId
    ) {
        apiKeyService.revoke(tenantId, apiKeyId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{apiKeyId}/rotate")
    public ResponseEntity<ApiKeyCreateResponse> rotate(
        @PathVariable UUID tenantId,
        @PathVariable UUID apiKeyId
    ) {
        ApiKeyCreateResponse response = apiKeyService.rotate(
            tenantId,
            apiKeyId
        );

        return ResponseEntity.ok(response);
    }
}
