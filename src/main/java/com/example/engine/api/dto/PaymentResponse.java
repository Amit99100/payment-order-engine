package com.example.engine.api.dto;
import com.example.engine.domain.Payment;
import java.math.BigDecimal;
import java.time.Instant;
public record PaymentResponse(Long id, Long orderId, BigDecimal amount, String status,
                              String gatewayReference, String failureReason, Instant createdAt) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getOrderId(), p.getAmount(), p.getStatus().name(),
            p.getGatewayReference(), p.getFailureReason(), p.getCreatedAt());
    }
}
