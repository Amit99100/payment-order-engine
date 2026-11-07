package com.example.engine.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Product name, SKU and unit price are snapshotted at purchase time.
 * If a product is renamed or repriced tomorrow, historical orders stay correct.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false, length = 100)
    private String sku;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    protected OrderItem() {}

    public OrderItem(CustomerOrder order, Long productId, String sku, String productName,
                     int quantity, BigDecimal unitPrice) {
        this.order = order; this.productId = productId; this.sku = sku;
        this.productName = productName; this.quantity = quantity; this.unitPrice = unitPrice;
    }

    public BigDecimal getLineTotal() { return unitPrice.multiply(BigDecimal.valueOf(quantity)); }
    public Long getProductId() { return productId; }
    public String getSku() { return sku; }
    public String getProductName() { return productName; }
    public int getQuantity() { return quantity; }
    public BigDecimal getUnitPrice() { return unitPrice; }
}
