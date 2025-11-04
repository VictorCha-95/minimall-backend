package com.minimall.domain.order.dto.request;

import com.minimall.domain.product.Product;

public record OrderItemCreateDto(
        Long productId,
        Integer quantity
) {
}
