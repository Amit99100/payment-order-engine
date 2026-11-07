package com.example.engine.domain;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * This row is inserted in the SAME database transaction as the business change.
 * A process crash after commit cannot erase the event; the scheduler can retry it.
 */
@Entity
@Table(name = "outbox_events", indexes = @Index(name = "idx_outbox_pending", columnList = "published, createdAt"))
public class OutboxEvent {
    @Id
    private UUID id = UUID.randomUUID();

    @Column(nullable = false, length = 100)
    private String aggregateType;

    @Column(nullable = false)
    private Long aggregateId;

    @Column(nullable = false, length = 100)
    private String eventType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false)
    private boolean published = false;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    private Instant publishedAt;

    public Long getAggregateId() {
        return aggregateId;
    }

    protected OutboxEvent() {}
    public OutboxEvent(String aggregateType, Long aggregateId, String eventType, String payload) {
        this.aggregateType = aggregateType; this.aggregateId = aggregateId;
        this.eventType = eventType; this.payload = payload;
    }
    public UUID getId() { return id; }
    public String getEventType() { return eventType; }
    public String getPayload() { return payload; }
    public boolean isPublished() { return published; }
    public void markPublished() { published = true; publishedAt = Instant.now(); }
}
