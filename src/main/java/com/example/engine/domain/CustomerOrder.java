package com.example.engine.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Order totals are calculated on the server from database product prices.
 * Never trust a total supplied by a client.
 */
@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String customerEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.CREATED;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    protected CustomerOrder() {}

    public CustomerOrder(String customerEmail) { this.customerEmail = customerEmail; }

    public void addItem(Product product, int quantity) {
        OrderItem item = new OrderItem(this, product.getId(), product.getSku(), product.getName(),
                quantity, product.getPrice());
        items.add(item);
        totalAmount = totalAmount.add(item.getLineTotal());
    }

    public void changeStatus(OrderStatus status) { this.status = status; }
    public Long getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public OrderStatus getStatus() { return status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderItem> getItems() { return List.copyOf(items); }
}
