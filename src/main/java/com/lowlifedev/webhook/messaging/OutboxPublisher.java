package com.lowlifedev.webhook.messaging;

import com.lowlifedev.webhook.model.OutboxMessage;
import com.lowlifedev.webhook.service.OutboxService;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
public class OutboxPublisher {

    private static final Logger log = LoggerFactory.getLogger(
        OutboxPublisher.class
    );

    private static final Duration SEND_TIMEOUT = Duration.ofSeconds(15);

    private static final long BASE_RETRY_MS = 1_000;
    private static final long MAX_RETRY_MS = 60_000;

    private final OutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final int batchSize;

    public OutboxPublisher(
        OutboxService outboxService,
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper,
        @Value("${app.outbox.batch-size:25}") int batchSize
    ) {
        this.outboxService = outboxService;
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.batchSize = batchSize;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:1000}")
    public void publishDueMessages() {
        try {
            List<OutboxMessage> messages = outboxService.claimBatch(batchSize);

            for (OutboxMessage message : messages) {
                publishOne(message);
            }
        } catch (Exception exception) {
            log.error(
                "Outbox polling failed; uncompleted rows remain recoverable",
                exception
            );
        }
    }

    private void publishOne(OutboxMessage message) {
        try {
            String payload = objectMapper.writeValueAsString(
                message.getPayload()
            );

            kafkaTemplate
                .send(message.getTopic(), message.getMessageKey(), payload)
                .get(SEND_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

            boolean marked = outboxService.markPublished(
                message.getId(),
                message.getLeaseToken(),
                Instant.now()
            );

            if (marked) {
                log.debug(
                    "Outbox message published: id={}, type={}, topic={}",
                    message.getId(),
                    message.getMessageType(),
                    message.getTopic()
                );
            } else {
                log.warn(
                    "Kafka acknowledged message, but its lease is no longer current: id={}",
                    message.getId()
                );
            }
        } catch (Exception exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }

            Instant retryAt = Instant.now().plusMillis(
                retryDelayMs(message.getPublishAttemptCount())
            );

            String error = describeError(exception);

            boolean scheduled = outboxService.scheduleRetry(
                message.getId(),
                message.getLeaseToken(),
                retryAt,
                error
            );

            if (scheduled) {
                log.warn(
                    "Outbox publication failed: id={}, nextAttemptAt={}, error={}",
                    message.getId(),
                    retryAt,
                    error
                );
            } else {
                log.warn(
                    "Outbox publication failed and lease was lost: id={}",
                    message.getId()
                );
            }
        }
    }

    private long retryDelayMs(int attemptCount) {
        int exponent = Math.min(Math.max(attemptCount - 1, 0), 20);

        long multiplier = 1L << exponent;

        long upperBound = Math.min(MAX_RETRY_MS, BASE_RETRY_MS * multiplier);

        long lowerBound = Math.max(1, upperBound / 2);

        return ThreadLocalRandom.current().nextLong(lowerBound, upperBound + 1);
    }

    private String describeError(Exception exception) {
        String message = exception.getMessage();

        String result =
            exception.getClass().getSimpleName() +
            (message == null ? "" : ": " + message);

        return result.substring(0, Math.min(result.length(), 1000));
    }
}
