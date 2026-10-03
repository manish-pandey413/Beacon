package com.lowlifedev.webhook.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "webhook_endpoints")
public class WebhookEndpoint {

    @Id
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 2048)
    private String url;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WebhookEndpointStatus status;

    @Column(name = "encrypted_secret", nullable = false)
    private String encryptedSecret;

    @Column(name = "secret_version", nullable = false)
    private int secretVersion;

    @Column(name = "timeout_ms", nullable = false)
    private int timeoutMs;

    @Column(name = "max_attempts", nullable = false)
    private int maxAttempts;

    @Column(name = "retry_base_delay_ms", nullable = false)
    private long retryBaseDelayMs;

    @Column(name = "retry_max_delay_ms", nullable = false)
    private long retryMaxDelayMs;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected WebhookEndpoint() {}

    public WebhookEndpoint(
        UUID id,
        UUID tenantId,
        String name,
        String url,
        String encryptedSecret,
        int timeoutMs,
        int maxAttempts,
        long retryBaseDelayMs,
        long retryMaxDelayMs
    ) {
        this.id = id;
        this.tenantId = tenantId;
        this.name = name;
        this.url = url;
        this.encryptedSecret = encryptedSecret;
        this.secretVersion = 1;
        this.status = WebhookEndpointStatus.ACTIVE;
        this.timeoutMs = timeoutMs;
        this.maxAttempts = maxAttempts;
        this.retryBaseDelayMs = retryBaseDelayMs;
        this.retryMaxDelayMs = retryMaxDelayMs;
    }

    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();

        if (createdAt == null) {
            createdAt = now;
        }

        if (updatedAt == null) {
            updatedAt = now;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }

    public void deactivate() {
        status = WebhookEndpointStatus.INACTIVE;
    }

    public UUID getId() {
        return id;
    }

    public UUID getTenantId() {
        return tenantId;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public WebhookEndpointStatus getStatus() {
        return status;
    }

    public String getEncryptedSecret() {
        return encryptedSecret;
    }

    public int getSecretVersion() {
        return secretVersion;
    }

    public int getTimeoutMs() {
        return timeoutMs;
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public long getRetryBaseDelayMs() {
        return retryBaseDelayMs;
    }

    public long getRetryMaxDelayMs() {
        return retryMaxDelayMs;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
