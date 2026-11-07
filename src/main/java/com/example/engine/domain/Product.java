package com.example.engine.domain;

import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Inventory is stored as a database row so concurrent application instances
 * coordinate through the database, not through a JVM-only synchronized block.
 */
@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String sku;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int availableQuantity;

    protected Product() {}

    public Product(String sku, String name, BigDecimal price, int availableQuantity) {
        this.sku = sku; this.name = name; this.price = price;
        this.availableQuantity = availableQuantity;
    }

    public void reserve(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        if (availableQuantity < quantity) throw new IllegalStateException("Insufficient inventory for SKU " + sku);
        availableQuantity -= quantity;
    }

    public void restock(int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive");
        availableQuantity += quantity;
    }

    public Long getId() { return id; }
    public String getSku() { return sku; }
    public String getName() { return name; }
    public BigDecimal getPrice() { return price; }
    public int getAvailableQuantity() { return availableQuantity; }
}
