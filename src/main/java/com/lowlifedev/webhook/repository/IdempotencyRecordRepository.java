package com.lowlifedev.webhook.repository;

import com.lowlifedev.webhook.model.IdempotencyRecord;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IdempotencyRecordRepository
    extends JpaRepository<IdempotencyRecord, UUID>
{
    @Modifying
    @Query(
        value = """
        DELETE FROM idempotency_records
        WHERE tenant_id = :tenantId
          AND idempotency_key = :idempotencyKey
          AND expires_at <= CURRENT_TIMESTAMP
        """,
        nativeQuery = true
    )
    int deleteExpiredForKey(
        @Param("tenantId") UUID tenantId,
        @Param("idempotencyKey") String idempotencyKey
    );

    @Modifying
    @Query(
        value = """
        INSERT INTO idempotency_records (
            id,
            tenant_id,
            idempotency_key,
            request_hash,
            event_id,
            created_at,
            expires_at
        )
        VALUES (
            :id,
            :tenantId,
            :idempotencyKey,
            :requestHash,
            NULL,
            CURRENT_TIMESTAMP,
            CURRENT_TIMESTAMP + INTERVAL '24 hours'
        )
        ON CONFLICT (tenant_id, idempotency_key)
        DO NOTHING
        """,
        nativeQuery = true
    )
    int tryCreate(
        @Param("id") UUID id,
        @Param("tenantId") UUID tenantId,
        @Param("idempotencyKey") String idempotencyKey,
        @Param("requestHash") String requestHash
    );

    Optional<IdempotencyRecord> findByTenantIdAndIdempotencyKey(
        UUID tenantId,
        String idempotencyKey
    );

    @Modifying
    @Query(
        value = """
        UPDATE idempotency_records
        SET event_id = :eventId
        WHERE tenant_id = :tenantId
          AND idempotency_key = :idempotencyKey
          AND event_id IS NULL
        """,
        nativeQuery = true
    )
    int attachEvent(
        @Param("tenantId") UUID tenantId,
        @Param("idempotencyKey") String idempotencyKey,
        @Param("eventId") UUID eventId
    );
}
