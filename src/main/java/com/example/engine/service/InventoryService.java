package com.example.engine.service;

import com.example.engine.domain.Product;
import com.example.engine.exception.NotFoundException;
import com.example.engine.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class InventoryService {
    private final ProductRepository products;
    public InventoryService(ProductRepository products) { this.products = products; }

    @Transactional
    public Product reserve(Long productId, int quantity) {
        Product product = products.findByIdForUpdate(productId)
            .orElseThrow(() -> new NotFoundException("Product " + productId + " was not found"));
        product.reserve(quantity);
        return product;
    }

    @Transactional
    public Product restock(Long productId, int quantity) {
        Product product = products.findByIdForUpdate(productId)
            .orElseThrow(() -> new NotFoundException("Product " + productId + " was not found"));
        product.restock(quantity);
        return product;
    }
}
