package com.example.engine.service;

import com.example.engine.domain.OutboxEvent;
import com.example.engine.repository.OutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Reads durable events from PostgreSQL and publishes them to Kafka.
 *
 * The event is marked published only after Kafka acknowledges it. If Kafka is
 * unavailable, the row remains pending and the next scheduled run retries it.
 *
 * Delivery is AT-LEAST-ONCE, not exactly-once across PostgreSQL and Kafka.
 * A crash after Kafka accepts the event but before this DB transaction commits
 * can cause a duplicate. Every consumer should therefore deduplicate by event ID.
 */
@Component
public class OutboxPublisher {
    private static final Logger log = LoggerFactory.getLogger(OutboxPublisher.class);
    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String topic;

    public OutboxPublisher(OutboxRepository repository,
                           KafkaTemplate<String, String> kafkaTemplate,
                           @Value("${app.kafka.topic}") String topic) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
        this.topic = topic;
    }

    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms:3000}")
    @Transactional
    public void publishPending() {
        List<OutboxEvent> pending = repository.findTop50ByPublishedFalseOrderByCreatedAtAsc();
        for (OutboxEvent event : pending) {
            try {
                // Use aggregate ID as key so events for one order are routed consistently.
                kafkaTemplate.send(topic, String.valueOf(event.getAggregateId()), event.getPayload())
                    .get(10, TimeUnit.SECONDS);
                event.markPublished();
                log.info("Published outbox event id={} type={} topic={}",
                    event.getId(), event.getEventType(), topic);
            } catch (Exception ex) {
                // Do not mark as published. A later scheduler execution retries it.
                log.error("Kafka publish failed; event remains pending id={}", event.getId(), ex);
            }
        }
    }
}
