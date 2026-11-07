package com.example.engine.api;

import com.example.engine.api.dto.*;
import com.example.engine.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderResponse.from(service.create(request)));
    }

    @GetMapping("/{id}")
    public OrderResponse get(@PathVariable Long id) {
        return OrderResponse.from(service.get(id));
    }
}
