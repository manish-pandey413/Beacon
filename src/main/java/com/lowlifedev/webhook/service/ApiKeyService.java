package com.lowlifedev.webhook.service;

import com.lowlifedev.webhook.dto.ApiKeyCreateRequest;
import com.lowlifedev.webhook.dto.ApiKeyCreateResponse;
import com.lowlifedev.webhook.dto.ApiKeyResponse;
import com.lowlifedev.webhook.exception.ApiKeyNotFoundException;
import com.lowlifedev.webhook.exception.TenantNotFoundException;
import com.lowlifedev.webhook.model.ApiKey;
import com.lowlifedev.webhook.repository.ApiKeyRepository;
import com.lowlifedev.webhook.repository.TenantRepository;
import com.lowlifedev.webhook.util.ApiKeyGenerator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiKeyService {

    private final ApiKeyRepository apiKeyRepository;
    private final TenantRepository tenantRepository;
    private final ApiKeyGenerator apiKeyGenerator;

    public ApiKeyService(
        ApiKeyRepository apiKeyRepository,
        TenantRepository tenantRepository,
        ApiKeyGenerator apiKeyGenerator
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.tenantRepository = tenantRepository;
        this.apiKeyGenerator = apiKeyGenerator;
    }

    @Transactional
    public ApiKeyCreateResponse create(
        UUID tenantId,
        ApiKeyCreateRequest request
    ) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }

        ApiKeyGenerator.GeneratedApiKey generated = apiKeyGenerator.generate();

        ApiKey apiKey = new ApiKey(
            tenantId,
            request.name(),
            generated.keyPrefix(),
            generated.hash(),
            request.expiresAt()
        );

        ApiKey saved = apiKeyRepository.save(apiKey);

        return new ApiKeyCreateResponse(
            saved.getId(),
            saved.getTenantId(),
            saved.getName(),
            saved.getKeyPrefix(),
            generated.rawKey(),
            saved.getStatus().name(),
            saved.getCreatedAt(),
            saved.getExpiresAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ApiKeyResponse> getAll(UUID tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }

        return apiKeyRepository
            .findAllByTenantIdOrderByCreatedAtDesc(tenantId)
            .stream()
            .map(ApiKeyResponse::from)
            .toList();
    }

    @Transactional
    public void revoke(UUID tenantId, UUID apiKeyId) {
        ApiKey apiKey = apiKeyRepository
            .findByTenantIdAndId(tenantId, apiKeyId)
            .orElseThrow(() -> new ApiKeyNotFoundException(apiKeyId));

        apiKey.revoke();
    }

    @Transactional
    public ApiKeyCreateResponse rotate(UUID tenantId, UUID apiKeyId) {
        ApiKey oldKey = apiKeyRepository
            .findByIdForUpdate(apiKeyId)
            .orElseThrow(() -> new ApiKeyNotFoundException(apiKeyId));

        if (!oldKey.getTenantId().equals(tenantId)) {
            throw new ApiKeyNotFoundException(apiKeyId);
        }

        if (!oldKey.isActive()) {
            throw new IllegalStateException(
                "Only an active API key can be rotated"
            );
        }

        oldKey.revoke();

        ApiKeyGenerator.GeneratedApiKey generated = apiKeyGenerator.generate();

        ApiKey newKey = new ApiKey(
            oldKey.getTenantId(),
            oldKey.getName(),
            generated.keyPrefix(),
            generated.hash(),
            oldKey.getExpiresAt()
        );

        ApiKey savedNewKey = apiKeyRepository.save(newKey);

        return new ApiKeyCreateResponse(
            savedNewKey.getId(),
            savedNewKey.getTenantId(),
            savedNewKey.getName(),
            savedNewKey.getKeyPrefix(),
            generated.rawKey(),
            savedNewKey.getStatus().name(),
            savedNewKey.getCreatedAt(),
            savedNewKey.getExpiresAt()
        );
    }
}
