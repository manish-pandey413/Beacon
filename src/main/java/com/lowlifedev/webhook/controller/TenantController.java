package com.lowlifedev.webhook.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lowlifedev.webhook.dto.TenantCreateRequest;
import com.lowlifedev.webhook.dto.TenantResponse;
import com.lowlifedev.webhook.service.TenantService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/v1/tenants")
public class TenantController {

    private final TenantService tenantService;

    public TenantController(TenantService tenantService) {
        this.tenantService = tenantService;
    }

    @PostMapping
    public ResponseEntity<TenantResponse> create( @Valid @RequestBody TenantCreateRequest request ) {

        TenantResponse response = tenantService.create(request);

        URI location = URI.create("/v1/tenants/" + response.id());

        return ResponseEntity
        .created(location)
        .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TenantResponse> getById( @PathVariable UUID id ) {
        return ResponseEntity.ok(
            tenantService.getById(id)
        );
    }
}
