package com.example.engine.api.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
public record CreateProductRequest(
    @NotBlank @Size(max=100) String sku,
    @NotBlank @Size(max=200) String name,
    @NotNull @DecimalMin("0.01") BigDecimal price,
    @Min(0) int availableQuantity
) {}
