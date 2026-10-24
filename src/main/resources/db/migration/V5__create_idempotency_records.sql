CREATE TABLE idempotency_records (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    idempotency_key VARCHAR(255) NOT NULL,

    request_hash VARCHAR(64) NOT NULL,

    event_id UUID,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    expires_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_idempotency_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_idempotency_event_tenant
        FOREIGN KEY (tenant_id, event_id)
        REFERENCES events(tenant_id, id),

    CONSTRAINT uq_idempotency_tenant_key
        UNIQUE (tenant_id, idempotency_key)
);

CREATE INDEX idx_idempotency_expiry
    ON idempotency_records (expires_at);
