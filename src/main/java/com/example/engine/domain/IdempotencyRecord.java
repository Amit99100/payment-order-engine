package com.example.engine.domain;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * The unique key constraint is the final authority under concurrent requests.
 * A cache lookup alone is not sufficient: two requests can both miss a cache.
 */
@Entity
@Table(name = "idempotency_records",
       uniqueConstraints = @UniqueConstraint(name = "uk_idempotency_key", columnNames = "idempotency_key"))
public class IdempotencyRecord {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "idempotency_key", nullable = false, length = 100)
    private String key;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long paymentId;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected IdempotencyRecord() {}
    public IdempotencyRecord(String key, Long orderId, Long paymentId) {
        this.key = key; this.orderId = orderId; this.paymentId = paymentId;
    }
    public String getKey() { return key; }
    public Long getOrderId() { return orderId; }
    public Long getPaymentId() { return paymentId; }
}
