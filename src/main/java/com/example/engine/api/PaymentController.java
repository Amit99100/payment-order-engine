package com.example.engine.api;

import com.example.engine.api.dto.PaymentResponse;
import com.example.engine.service.PaymentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders/{orderId}/payments")
public class PaymentController {
    private final PaymentService service;
    public PaymentController(PaymentService service) { this.service = service; }

    @PostMapping
    public PaymentResponse pay(@PathVariable Long orderId,
        @RequestHeader("Idempotency-Key") String idempotencyKey) {
        return service.pay(orderId, idempotencyKey);
    }
}
