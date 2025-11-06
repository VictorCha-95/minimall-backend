package com.minimall.domain.order.dto.request;

import com.minimall.domain.product.Product;
import jakarta.validation.constraints.NotNull;

public record OrderItemCreateDto(
        @NotNull Long productId,
        @NotNull Integer quantity
) {
}
