ALTER TABLE webhook_endpoints
    ADD CONSTRAINT uq_webhook_endpoints_tenant_id
    UNIQUE (tenant_id, id);


CREATE TABLE events (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    event_type VARCHAR(100) NOT NULL,

    version INTEGER NOT NULL DEFAULT 1,

    ordering_key VARCHAR(255),

    data JSONB NOT NULL,

    metadata JSONB NOT NULL DEFAULT '{}'::jsonb,

    occurred_at TIMESTAMPTZ,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_events_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT chk_events_version
        CHECK (version > 0),

    CONSTRAINT uq_events_tenant_id
        UNIQUE (tenant_id, id)
);


CREATE INDEX idx_events_tenant_created
    ON events (tenant_id, created_at DESC);

CREATE INDEX idx_events_tenant_type_created
    ON events (tenant_id, event_type, created_at DESC);


CREATE TABLE deliveries (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    event_id UUID NOT NULL,

    endpoint_id UUID NOT NULL,

    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',

    attempt_count INTEGER NOT NULL DEFAULT 0,

    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    lease_expires_at TIMESTAMPTZ,

    last_error TEXT,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    completed_at TIMESTAMPTZ,

    CONSTRAINT fk_deliveries_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT fk_deliveries_event_tenant
        FOREIGN KEY (tenant_id, event_id)
        REFERENCES events(tenant_id, id),

    CONSTRAINT fk_deliveries_endpoint_tenant
        FOREIGN KEY (tenant_id, endpoint_id)
        REFERENCES webhook_endpoints(tenant_id, id),

    CONSTRAINT uq_deliveries_event_endpoint
        UNIQUE (event_id, endpoint_id),

    CONSTRAINT chk_deliveries_status
        CHECK (
            status IN (
                'PENDING',
                'IN_PROGRESS',
                'SUCCEEDED',
                'RETRY_SCHEDULED',
                'DEAD_LETTERED'
            )
        ),

    CONSTRAINT chk_deliveries_attempt_count
        CHECK (attempt_count >= 0)
);


CREATE INDEX idx_deliveries_tenant_created
    ON deliveries (tenant_id, created_at DESC);

CREATE INDEX idx_deliveries_endpoint_created
    ON deliveries (endpoint_id, created_at DESC);

CREATE INDEX idx_deliveries_due
    ON deliveries (next_attempt_at)
    WHERE status IN ('PENDING', 'RETRY_SCHEDULED');
