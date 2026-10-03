package com.lowlifedev.webhook.service;

import com.lowlifedev.webhook.dto.TenantCreateRequest;
import com.lowlifedev.webhook.dto.TenantResponse;
import com.lowlifedev.webhook.exception.TenantAlreadyExistsException;
import com.lowlifedev.webhook.exception.TenantNotFoundException;
import com.lowlifedev.webhook.model.Tenant;
import com.lowlifedev.webhook.repository.TenantRepository;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TenantService {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Transactional
    public TenantResponse create(TenantCreateRequest request) {
        if (tenantRepository.existsBySlug(request.slug())) {
            throw new TenantAlreadyExistsException(request.slug());
        }

        Tenant tenant = new Tenant(request.name(), request.slug());

        try {
            Tenant savedTenant = tenantRepository.save(tenant);

            return TenantResponse.from(savedTenant);
        } catch (DataIntegrityViolationException exception) {
            throw new TenantAlreadyExistsException(request.slug());
        }
    }

    @Transactional(readOnly = true)
    public TenantResponse getById(UUID id) {
        Tenant tenant = tenantRepository
            .findById(id)
            .orElseThrow(() -> new TenantNotFoundException(id));

        return TenantResponse.from(tenant);
    }
}
