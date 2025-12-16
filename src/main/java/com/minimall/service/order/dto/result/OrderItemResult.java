package com.minimall.service.order.dto;

public record OrderItemResult(
        Long productId,
        String productName,
        Integer orderPrice,
        Integer orderQuantity,
        Integer totalAmount
) {
}
