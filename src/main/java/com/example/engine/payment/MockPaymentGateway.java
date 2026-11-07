package com.example.engine.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Deterministic mock gateway. Configure app.payment.failure-rate-percent=100
 * to simulate a provider outage. A real implementation would use WebClient
 * and the provider's own idempotency support.
 */
@Component
public class MockPaymentGateway implements PaymentGateway {
    private final int failureRate;
    public MockPaymentGateway(@Value("${app.payment.failure-rate-percent:0}") int failureRate) {
        this.failureRate = Math.max(0, Math.min(100, failureRate));
    }
    @Override public GatewayResult charge(String key, long amountInMinorUnits) {
        if (ThreadLocalRandom.current().nextInt(100) < failureRate)
            return new GatewayResult(false, null, "Simulated payment gateway failure");
        return new GatewayResult(true, "mock_" + UUID.randomUUID(), null);
    }
}
