package com.lowlifedev.webhook.service;

import com.lowlifedev.webhook.model.OutboxMessage;
import com.lowlifedev.webhook.repository.OutboxMessageRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OutboxService {

    private final OutboxMessageRepository repository;
    private final long leaseSeconds;

    public OutboxService(
        OutboxMessageRepository repository,
        @Value("${app.outbox.lease-seconds:120}") long leaseSeconds
    ) {
        this.repository = repository;
        this.leaseSeconds = leaseSeconds;
    }

    @Transactional
    public List<OutboxMessage> claimBatch(int batchSize) {
        Instant now = Instant.now();

        List<OutboxMessage> messages = repository.findClaimable(now, batchSize);

        for (OutboxMessage message : messages) {
            message.claim(
                UUID.randomUUID(),
                now,
                now.plus(leaseSeconds, ChronoUnit.SECONDS)
            );
        }

        // Dirty checking persists the state changes at commit.
        return List.copyOf(messages);
    }

    @Transactional
    public boolean markPublished(
        UUID messageId,
        UUID leaseToken,
        Instant publishedAt
    ) {
        return (
            repository.markPublished(messageId, leaseToken, publishedAt) == 1
        );
    }

    @Transactional
    public boolean scheduleRetry(
        UUID messageId,
        UUID leaseToken,
        Instant nextAttemptAt,
        String lastError
    ) {
        return (
            repository.scheduleRetry(
                messageId,
                leaseToken,
                nextAttemptAt,
                lastError
            ) == 1
        );
    }
}
