package com.example.engine.service;

import com.example.engine.api.dto.CreateOrderRequest;
import com.example.engine.domain.*;
import com.example.engine.exception.NotFoundException;
import com.example.engine.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OrderService {
    private final OrderRepository orders;
    private final ProductRepository products;
    private final InventoryService inventory;
    private final OutboxService outbox;

    public OrderService(OrderRepository orders, ProductRepository products,
                        InventoryService inventory, OutboxService outbox) {
        this.orders = orders; this.products = products; this.inventory = inventory; this.outbox = outbox;
    }

    /**
     * All inventory reservations and order writes participate in one transaction.
     * If any item is unavailable, throwing an exception rolls back earlier reservations.
     * Sorting product IDs reduces deadlock risk when two orders contain the same SKUs
     * in different request order (both transactions lock rows in the same order).
     */
    @Transactional
    public CustomerOrder create(CreateOrderRequest request) {
        CustomerOrder order = new CustomerOrder(request.customerEmail());

        Map<Long, Integer> quantities = request.items().stream().collect(Collectors.toMap(
            CreateOrderRequest.OrderLineRequest::productId,
            CreateOrderRequest.OrderLineRequest::quantity,
            Integer::sum));

        for (var entry : new TreeMap<>(quantities).entrySet()) {
            Product product = inventory.reserve(entry.getKey(), entry.getValue());
            order.addItem(product, entry.getValue());
        }

        order.changeStatus(OrderStatus.PAYMENT_PENDING);
        CustomerOrder saved = orders.save(order);
        // Persist event in this transaction; it cannot be lost between DB commit and publish.
        outbox.add("ORDER", saved.getId(), "OrderCreated",
            "{\"orderId\":" + saved.getId() + ",\"totalAmount\":\"" + saved.getTotalAmount() + "\"}");
        return saved;
    }

    @Transactional(readOnly = true)
    public CustomerOrder get(Long id) {
        return orders.findWithItemsById(id).orElseThrow(() -> new NotFoundException("Order " + id + " was not found"));
    }
}
