package com.example.engine.service;

import com.example.engine.api.dto.PaymentResponse;
import com.example.engine.domain.*;
import com.example.engine.exception.*;
import com.example.engine.payment.*;
import com.example.engine.repository.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.math.BigDecimal;
import java.time.Duration;

@Service
public class PaymentService {
    private final OrderRepository orders;
    private final PaymentRepository payments;
    private final IdempotencyRepository idempotency;
    private final PaymentGateway gateway;
    private final SimpleCircuitBreaker breaker;
    private final OutboxService outbox;
    private final StringRedisTemplate redis;
    private final Duration idempotencyTtl;

    public PaymentService(OrderRepository orders, PaymentRepository payments,
                          IdempotencyRepository idempotency, PaymentGateway gateway,
                          SimpleCircuitBreaker breaker, OutboxService outbox,
                          StringRedisTemplate redis,
                          @Value("${app.redis.idempotency-ttl-hours:24}") long ttlHours) {
        this.orders = orders;
        this.payments = payments;
        this.idempotency = idempotency;
        this.gateway = gateway;
        this.breaker = breaker;
        this.outbox = outbox;
        this.redis = redis;
        this.idempotencyTtl = Duration.ofHours(ttlHours);
    }

    /**
     * Redis is the fast lookup layer; PostgreSQL remains the durable source of truth.
     * We write the cache only AFTER the DB transaction commits. Otherwise a rollback
     * could leave Redis pointing at a payment row that was never committed.
     *
     * A unique constraint in PostgreSQL is still essential: Redis alone cannot
     * safely arbitrate two simultaneous requests that both miss the cache.
     */
    @Transactional
    public PaymentResponse pay(Long orderId, String key) {
        if (key == null || key.isBlank() || key.length() > 100)
            throw new BusinessException("A valid Idempotency-Key header is required");

        String redisKey = "payment:idempotency:" + key;

        // Fast path: previously completed request can be served from Redis.
        String cachedPaymentId = redis.opsForValue().get(redisKey);
        if (cachedPaymentId != null) {
            Payment cached = payments.findById(Long.valueOf(cachedPaymentId))
                .orElseThrow(() -> new IllegalStateException("Redis points to a missing payment"));
            if (!cached.getOrderId().equals(orderId))
                throw new BusinessException("This idempotency key was already used for another order");
            return PaymentResponse.from(cached);
        }

        // Durable fallback: survives Redis eviction, restart or TTL expiry.
        var previous = idempotency.findByKey(key);
        if (previous.isPresent()) {
            if (!previous.get().getOrderId().equals(orderId))
                throw new BusinessException("This idempotency key was already used for another order");
            Payment original = payments.findById(previous.get().getPaymentId())
                .orElseThrow(() -> new IllegalStateException("Idempotency record has no payment"));
            cacheAfterCommit(redisKey, original.getId());
            return PaymentResponse.from(original);
        }

        CustomerOrder order = orders.findById(orderId)
            .orElseThrow(() -> new NotFoundException("Order " + orderId + " was not found"));

        if (order.getStatus() == OrderStatus.PAID)
            return payments.findByOrderId(orderId).map(PaymentResponse::from)
                .orElseThrow(() -> new IllegalStateException("Paid order has no payment record"));

        if (order.getStatus() != OrderStatus.PAYMENT_PENDING)
            throw new BusinessException("Order is not eligible for payment");

        if (payments.findByOrderId(orderId).isPresent())
            throw new BusinessException("A payment attempt already exists for this order");

        Payment payment = payments.save(new Payment(orderId, order.getTotalAmount()));
        idempotency.save(new IdempotencyRecord(key, orderId, payment.getId()));

        try {
            breaker.beforeCall();
            long minorUnits = order.getTotalAmount().multiply(new BigDecimal("100")).longValueExact();
            PaymentGateway.GatewayResult result = gateway.charge(key, minorUnits);
            breaker.record(result.successful());

            if (result.successful()) {
                payment.succeed(result.reference());
                order.changeStatus(OrderStatus.PAID);
                outbox.add("PAYMENT", payment.getId(), "PaymentSucceeded",
                        "{\"eventId\":\"payment-failed-" + payment.getId()
                                + "\",\"paymentId\":" + payment.getId()
                                + ",\"orderId\":" + orderId + "}");
            } else {
                payment.fail(result.failureReason());
                order.changeStatus(OrderStatus.PAYMENT_FAILED);
                outbox.add("PAYMENT", payment.getId(), "PaymentFailed",
                        "{\"eventId\":\"payment-" + payment.getId()
                                + "\",\"paymentId\":" + payment.getId()
                                + ",\"orderId\":" + orderId + "}");
            }
        } catch (IllegalStateException ex) {
            payment.fail(ex.getMessage());
            order.changeStatus(OrderStatus.PAYMENT_FAILED);
        }

        cacheAfterCommit(redisKey, payment.getId());
        return PaymentResponse.from(payment);
    }

    private void cacheAfterCommit(String redisKey, Long paymentId) {
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                redis.opsForValue().set(redisKey, String.valueOf(paymentId), idempotencyTtl);
            }
        });
    }
}
