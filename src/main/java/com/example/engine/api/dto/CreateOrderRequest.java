package com.example.engine.api.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
public record CreateOrderRequest(
    @NotBlank @Email String customerEmail,
    @NotEmpty List<@Valid OrderLineRequest> items
) {
    public record OrderLineRequest(@NotNull @Positive Long productId, @Positive int quantity) {}
}
