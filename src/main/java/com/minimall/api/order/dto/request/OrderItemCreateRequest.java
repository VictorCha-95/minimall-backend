package com.minimall.controller.api.order.dto.request;

import jakarta.validation.constraints.NotNull;

public record OrderItemCreateRequest(
        @NotNull Long productId,
        @NotNull Integer quantity
) {
}
