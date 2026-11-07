package com.example.engine.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Example downstream consumer.
 *
 * This listener only logs events to demonstrate the end-to-end Kafka path.
 * Real consumers should persist their own processing result and use the event
 * ID for deduplication because outbox delivery can happen more than once.
 */
@Component
public class OrderEventConsumer {
    private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);

    @KafkaListener(topics = "${app.kafka.topic}", groupId = "payment-order-audit")
    public void consume(String payload) {
        log.info("KAFKA_EVENT_RECEIVED payload={}", payload);
    }
}
