package com.example.engine.api.dto;
import com.example.engine.domain.CustomerOrder;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
public record OrderResponse(Long id, String customerEmail, String status, BigDecimal totalAmount,
                            Instant createdAt, List<Item> items) {


    public record Item(Long productId, String sku, String productName, int quantity,
                       BigDecimal unitPrice, BigDecimal lineTotal) {}



    public static OrderResponse from(CustomerOrder o) {
        return new OrderResponse(o.getId(), o.getCustomerEmail(), o.getStatus().name(),
            o.getTotalAmount(), o.getCreatedAt(),
            o.getItems().stream().map(i -> new Item(i.getProductId(), i.getSku(), i.getProductName(),
                i.getQuantity(), i.getUnitPrice(), i.getLineTotal())).toList());
    }
}
