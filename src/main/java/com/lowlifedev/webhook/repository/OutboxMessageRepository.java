package com.lowlifedev.webhook.repository;

import com.lowlifedev.webhook.model.OutboxMessage;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutboxMessageRepository
    extends JpaRepository<OutboxMessage, UUID>
{
    @Query(
        value = """
        SELECT *
        FROM outbox_messages
        WHERE (
            status = 'PENDING'
            AND next_attempt_at <= :now
        )
        OR (
            status = 'IN_PROGRESS'
            AND lease_expires_at <= :now
        )
        ORDER BY created_at ASC, id ASC
        LIMIT :batchSize
        FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true
    )
    List<OutboxMessage> findClaimable(
        @Param("now") Instant now,
        @Param("batchSize") int batchSize
    );

    @Modifying
    @Query(
        value = """
        UPDATE outbox_messages
        SET status = 'PUBLISHED',
            published_at = :publishedAt,
            lease_expires_at = NULL,
            lease_token = NULL,
            last_error = NULL
        WHERE id = :id
          AND status = 'IN_PROGRESS'
          AND lease_token = :leaseToken
        """,
        nativeQuery = true
    )
    int markPublished(
        @Param("id") UUID id,
        @Param("leaseToken") UUID leaseToken,
        @Param("publishedAt") Instant publishedAt
    );

    @Modifying
    @Query(
        value = """
        UPDATE outbox_messages
        SET status = 'PENDING',
            next_attempt_at = :nextAttemptAt,
            lease_expires_at = NULL,
            lease_token = NULL,
            last_error = :lastError
        WHERE id = :id
          AND status = 'IN_PROGRESS'
          AND lease_token = :leaseToken
        """,
        nativeQuery = true
    )
    int scheduleRetry(
        @Param("id") UUID id,
        @Param("leaseToken") UUID leaseToken,
        @Param("nextAttemptAt") Instant nextAttemptAt,
        @Param("lastError") String lastError
    );
}
