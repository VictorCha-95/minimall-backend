package com.minimall.domain.order.dto.response;

import jakarta.validation.constraints.NotNull;

public record OrderItemResponseDto(
        Long productId,
        String productName,
        Integer orderPrice,
        Integer orderQuantity,
        Integer totalAmount
) {
}
