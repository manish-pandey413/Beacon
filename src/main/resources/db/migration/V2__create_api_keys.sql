CREATE TABLE api_keys (
    id UUID PRIMARY KEY,

    tenant_id UUID NOT NULL,

    name VARCHAR(100) NOT NULL,

    key_prefix VARCHAR(20) NOT NULL,

    key_hash VARCHAR(64) NOT NULL,

    status VARCHAR(20) NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    revoked_at TIMESTAMPTZ,

    expires_at TIMESTAMPTZ,

    last_used_at TIMESTAMPTZ,

    CONSTRAINT fk_api_keys_tenant
        FOREIGN KEY (tenant_id)
        REFERENCES tenants(id),

    CONSTRAINT uq_api_keys_hash
        UNIQUE (key_hash),

    CONSTRAINT chk_api_keys_status
        CHECK (status IN ('ACTIVE', 'REVOKED'))
);

CREATE INDEX idx_api_keys_tenant
    ON api_keys (tenant_id);

CREATE INDEX idx_api_keys_tenant_status
    ON api_keys (tenant_id, status);
