package com.example.engine.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

/** One payment attempt record. Idempotency is enforced separately by a unique key. */
@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(name = "uk_payment_order", columnNames = "order_id"))
public class Payment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(length = 500)
    private String gatewayReference;

    @Column(length = 500)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    protected Payment() {}

    public Payment(Long orderId, BigDecimal amount) {
        this.orderId = orderId; this.amount = amount; this.status = PaymentStatus.INITIATED;
    }

    public void succeed(String reference) { status = PaymentStatus.SUCCEEDED; gatewayReference = reference; failureReason = null; }
    public void fail(String reason) { status = PaymentStatus.FAILED; failureReason = reason; }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public PaymentStatus getStatus() { return status; }
    public String getGatewayReference() { return gatewayReference; }
    public String getFailureReason() { return failureReason; }
    public Instant getCreatedAt() { return createdAt; }
}
