package com.minimall.service.order.dto;

import jakarta.validation.constraints.NotNull;

public record OrderItemCreateCommand(
        @NotNull Long productId,
        @NotNull Integer quantity
) {
}
