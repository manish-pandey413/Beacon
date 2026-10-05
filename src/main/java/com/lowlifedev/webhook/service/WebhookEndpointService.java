package com.lowlifedev.webhook.service;

import com.lowlifedev.webhook.dto.WebhookEndpointCreateRequest;
import com.lowlifedev.webhook.dto.WebhookEndpointCreateResponse;
import com.lowlifedev.webhook.dto.WebhookEndpointResponse;
import com.lowlifedev.webhook.exception.TenantNotFoundException;
import com.lowlifedev.webhook.exception.WebhookEndpointNotFoundException;
import com.lowlifedev.webhook.model.WebhookEndpoint;
import com.lowlifedev.webhook.repository.TenantRepository;
import com.lowlifedev.webhook.repository.WebhookEndpointRepository;
import com.lowlifedev.webhook.security.WebhookSecretCrypto;
import com.lowlifedev.webhook.util.WebhookSecretGenerator;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WebhookEndpointService {

    private static final int DEFAULT_TIMEOUT_MS = 10_000;
    private static final int DEFAULT_MAX_ATTEMPTS = 5;
    private static final long DEFAULT_RETRY_BASE_DELAY_MS = 1_000;
    private static final long DEFAULT_RETRY_MAX_DELAY_MS = 600_000;

    private final WebhookEndpointRepository endpointRepository;
    private final TenantRepository tenantRepository;
    private final WebhookSecretGenerator secretGenerator;
    private final WebhookSecretCrypto secretCrypto;

    public WebhookEndpointService(
        WebhookEndpointRepository endpointRepository,
        TenantRepository tenantRepository,
        WebhookSecretGenerator secretGenerator,
        WebhookSecretCrypto secretCrypto
    ) {
        this.endpointRepository = endpointRepository;
        this.tenantRepository = tenantRepository;
        this.secretGenerator = secretGenerator;
        this.secretCrypto = secretCrypto;
    }

    @Transactional
    public WebhookEndpointCreateResponse create(
        UUID tenantId,
        WebhookEndpointCreateRequest request
    ) {
        ensureTenantExists(tenantId);

        validateUrl(request.url());

        int timeoutMs =
            request.timeoutMs() != null
                ? request.timeoutMs()
                : DEFAULT_TIMEOUT_MS;

        int maxAttempts =
            request.maxAttempts() != null
                ? request.maxAttempts()
                : DEFAULT_MAX_ATTEMPTS;

        long retryBaseDelayMs =
            request.retryBaseDelayMs() != null
                ? request.retryBaseDelayMs()
                : DEFAULT_RETRY_BASE_DELAY_MS;

        long retryMaxDelayMs =
            request.retryMaxDelayMs() != null
                ? request.retryMaxDelayMs()
                : DEFAULT_RETRY_MAX_DELAY_MS;

        if (retryMaxDelayMs < retryBaseDelayMs) {
            throw new IllegalArgumentException(
                "retryMaxDelayMs must be >= retryBaseDelayMs"
            );
        }

        String secret = secretGenerator.generate();

        String encryptedSecret = secretCrypto.encrypt(secret);

        UUID endpointId = UUID.randomUUID();

        WebhookEndpoint endpoint = new WebhookEndpoint(
            endpointId,
            tenantId,
            request.name(),
            request.url(),
            encryptedSecret,
            timeoutMs,
            maxAttempts,
            retryBaseDelayMs,
            retryMaxDelayMs
        );

        WebhookEndpoint saved = endpointRepository.save(endpoint);

        return new WebhookEndpointCreateResponse(
            saved.getId(),
            saved.getTenantId(),
            saved.getName(),
            saved.getUrl(),
            secret,
            saved.getStatus().name(),
            saved.getSecretVersion(),
            saved.getTimeoutMs(),
            saved.getMaxAttempts(),
            saved.getRetryBaseDelayMs(),
            saved.getRetryMaxDelayMs(),
            saved.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<WebhookEndpointResponse> getAll(UUID tenantId) {
        ensureTenantExists(tenantId);

        return endpointRepository
            .findAllByTenantIdOrderByCreatedAtDesc(tenantId)
            .stream()
            .map(WebhookEndpointResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public WebhookEndpointResponse getById(UUID tenantId, UUID endpointId) {
        return WebhookEndpointResponse.from(
            getOwnedEndpoint(tenantId, endpointId)
        );
    }

    @Transactional
    public void deactivate(UUID tenantId, UUID endpointId) {
        WebhookEndpoint endpoint = getOwnedEndpoint(tenantId, endpointId);

        endpoint.deactivate();
    }

    private WebhookEndpoint getOwnedEndpoint(UUID tenantId, UUID endpointId) {
        return endpointRepository
            .findByTenantIdAndId(tenantId, endpointId)
            .orElseThrow(() ->
                new WebhookEndpointNotFoundException(endpointId)
            );
    }

    private void ensureTenantExists(UUID tenantId) {
        if (!tenantRepository.existsById(tenantId)) {
            throw new TenantNotFoundException(tenantId);
        }
    }

    private void validateUrl(String value) {
        try {
            URI uri = URI.create(value);

            String scheme = uri.getScheme();

            if (
                !"http".equalsIgnoreCase(scheme) &&
                !"https".equalsIgnoreCase(scheme)
            ) {
                throw new IllegalArgumentException(
                    "Webhook URL must use HTTP or HTTPS"
                );
            }

            if (uri.getHost() == null) {
                throw new IllegalArgumentException(
                    "Webhook URL must contain a valid host"
                );
            }
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                "Invalid webhook URL",
                exception
            );
        }
    }
}
