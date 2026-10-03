CREATE TABLE webhook_endpoints (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    name VARCHAR(100) NOT NULL,

    url VARCHAR(2048) NOT NULL,

    status VARCHAR(20) NOT NULL,

    encrypted_secret TEXT NOT NULL,

    secret_version INTEGER NOT NULL DEFAULT 1,

    timeout_ms INTEGER NOT NULL DEFAULT 10000,

    max_attempts INTEGER NOT NULL DEFAULT 5,

    retry_base_delay_ms BIGINT NOT NULL DEFAULT 1000,

    retry_max_delay_ms BIGINT NOT NULL DEFAULT 600000,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_webhook_endpoints_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT chk_webhook_endpoints_status
        CHECK (status IN ('ACTIVE', 'INACTIVE')),

    CONSTRAINT chk_webhook_endpoints_timeout
        CHECK (timeout_ms > 0),

    CONSTRAINT chk_webhook_endpoints_attempts
        CHECK (max_attempts > 0),

    CONSTRAINT chk_webhook_endpoints_retry_base
        CHECK (retry_base_delay_ms >= 0),

    CONSTRAINT chk_webhook_endpoints_retry_max
        CHECK (retry_max_delay_ms >= retry_base_delay_ms)
);

CREATE INDEX idx_webhook_endpoints_tenant
    ON webhook_endpoints (tenant_id);

CREATE INDEX idx_webhook_endpoints_tenant_status
    ON webhook_endpoints (tenant_id, status);


CREATE TABLE subscriptions (
    id UUID PRIMARY KEY,

    endpoint_id UUID NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    active BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_subscriptions_endpoint
        FOREIGN KEY (endpoint_id)
        REFERENCES webhook_endpoints(id),

    CONSTRAINT uq_subscriptions_endpoint_event
        UNIQUE (endpoint_id, event_type)
);

CREATE INDEX idx_subscriptions_endpoint
    ON subscriptions (endpoint_id);

CREATE INDEX idx_subscriptions_endpoint_active
    ON subscriptions (endpoint_id, active);
