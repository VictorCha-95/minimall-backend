package com.minimall.api.order.dto.response;

public record OrderItemResponse(
        Long productId,
        String productName,
        Integer orderPrice,
        Integer orderQuantity,
        Integer totalAmount
) {
}
