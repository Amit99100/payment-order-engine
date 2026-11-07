package com.example.engine.payment;

import org.springframework.stereotype.Component;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Small educational circuit breaker, intentionally implemented with Java.
 * It tracks the most recent 10 outcomes. At 5 failures the circuit opens for
 * 10 seconds. This is process-local, so production multi-instance deployments
 * should use a mature library or a shared coordination mechanism.
 */
@Component
public class SimpleCircuitBreaker {
    private final Deque<Boolean> outcomes = new ArrayDeque<>();
    private Instant openedAt;
    private static final int WINDOW_SIZE = 10;
    private static final int FAILURE_THRESHOLD = 5;
    private static final Duration OPEN_DURATION = Duration.ofSeconds(10);

    public synchronized void beforeCall() {
        if (openedAt == null) return;
        if (Instant.now().isBefore(openedAt.plus(OPEN_DURATION)))
            throw new IllegalStateException("Payment gateway circuit is OPEN; retry later");
        // After cool-down, allow a trial request (half-open behavior).
        openedAt = null;
    }

    public synchronized void record(boolean success) {
        outcomes.addLast(success);
        if (outcomes.size() > WINDOW_SIZE) outcomes.removeFirst();
        long failures = outcomes.stream().filter(result -> !result).count();
        if (outcomes.size() == WINDOW_SIZE && failures >= FAILURE_THRESHOLD)
            openedAt = Instant.now();
    }
}
