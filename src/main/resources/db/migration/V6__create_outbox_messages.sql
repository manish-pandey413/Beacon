CREATE TABLE outbox_messages (
    id UUID PRIMARY KEY,

    message_type VARCHAR(100) NOT NULL,

    topic VARCHAR(200) NOT NULL,

    message_key VARCHAR(512) NOT NULL,

    aggregate_id UUID NOT NULL,

    payload JSONB NOT NULL,

    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',

    publish_attempt_count INTEGER NOT NULL DEFAULT 0,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    next_attempt_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    lease_expires_at TIMESTAMPTZ,

    lease_token UUID,

    published_at TIMESTAMPTZ,

    last_error TEXT,

    CONSTRAINT chk_outbox_status
        CHECK (status IN ('PENDING', 'IN_PROGRESS', 'PUBLISHED')),

    CONSTRAINT chk_outbox_attempt_count
        CHECK (publish_attempt_count >= 0),

    CONSTRAINT chk_outbox_lease
        CHECK (
            (
                status = 'IN_PROGRESS'
                AND lease_token IS NOT NULL
                AND lease_expires_at IS NOT NULL
            )
            OR
            (
                status <> 'IN_PROGRESS'
                AND lease_token IS NULL
                AND lease_expires_at IS NULL
            )
        ),

    CONSTRAINT chk_outbox_published_at
        CHECK (
            (status = 'PUBLISHED' AND published_at IS NOT NULL)
            OR
            (status <> 'PUBLISHED' AND published_at IS NULL)
        )
);

CREATE INDEX idx_outbox_pending
    ON outbox_messages (next_attempt_at, created_at)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_expired_leases
    ON outbox_messages (lease_expires_at, created_at)
    WHERE status = 'IN_PROGRESS';
