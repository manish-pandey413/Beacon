package com.lowlifedev.webhook.service;

import com.lowlifedev.webhook.exception.InvalidApiKeyException;
import com.lowlifedev.webhook.model.ApiKey;
import com.lowlifedev.webhook.model.Tenant;
import com.lowlifedev.webhook.model.TenantStatus;
import com.lowlifedev.webhook.repository.ApiKeyRepository;
import com.lowlifedev.webhook.repository.TenantRepository;
import com.lowlifedev.webhook.security.ApiKeyHasher;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiKeyAuthenticationService {

    private final ApiKeyRepository apiKeyRepository;
    private final TenantRepository tenantRepository;
    private final ApiKeyHasher apiKeyHasher;

    public ApiKeyAuthenticationService(
        ApiKeyRepository apiKeyRepository,
        TenantRepository tenantRepository,
        ApiKeyHasher apiKeyHasher
    ) {
        this.apiKeyRepository = apiKeyRepository;
        this.tenantRepository = tenantRepository;
        this.apiKeyHasher = apiKeyHasher;
    }

    @Transactional(readOnly = true)
    public UUID authenticate(String authorizationHeader) {
        if (
            authorizationHeader == null ||
            !authorizationHeader.regionMatches(true, 0, "Bearer ", 0, 7)
        ) {
            throw new InvalidApiKeyException();
        }

        String rawKey = authorizationHeader.substring(7).trim();

        if (!rawKey.startsWith("wk_live_")) {
            throw new InvalidApiKeyException();
        }

        String keyHash = apiKeyHasher.hash(rawKey);

        ApiKey apiKey = apiKeyRepository
            .findByKeyHash(keyHash)
            .orElseThrow(InvalidApiKeyException::new);

        if (!apiKey.isActive()) {
            throw new InvalidApiKeyException();
        }

        Tenant tenant = tenantRepository
            .findById(apiKey.getTenantId())
            .orElseThrow(InvalidApiKeyException::new);

        if (tenant.getStatus() != TenantStatus.ACTIVE) {
            throw new InvalidApiKeyException();
        }

        return tenant.getId();
    }
}
