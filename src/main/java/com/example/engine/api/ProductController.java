package com.example.engine.api;

import com.example.engine.api.dto.CreateProductRequest;
import com.example.engine.domain.Product;
import com.example.engine.exception.NotFoundException;
import com.example.engine.repository.ProductRepository;
import com.example.engine.service.InventoryService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {


    private final ProductRepository products;
    private final InventoryService inventory;
    public ProductController(ProductRepository products, InventoryService inventory) {
        this.products = products; this.inventory = inventory;
    }

    @PostMapping
    public ResponseEntity<Product> create(@Valid @RequestBody CreateProductRequest request) {
        Product saved = products.save(new Product(request.sku(), request.name(),
            request.price(), request.availableQuantity()));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/{id}")
    public Product get(@PathVariable Long id) {
        return products.findById(id).orElseThrow(() -> new NotFoundException("Product " + id + " was not found"));
    }

    @PostMapping("/{id}/restock")
    public Product restock(@PathVariable Long id, @RequestParam @Positive int quantity) {
        return inventory.restock(id, quantity);
    }
}
