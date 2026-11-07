package com.example.engine.payment;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SimpleCircuitBreakerTest {
    @Test
    void opensAfterFiveFailuresInTenRecordedCalls() {
        SimpleCircuitBreaker breaker = new SimpleCircuitBreaker();
        for (int i = 0; i < 5; i++) breaker.record(false);
        for (int i = 0; i < 5; i++) breaker.record(true);
        assertThrows(IllegalStateException.class, breaker::beforeCall);
    }
}
