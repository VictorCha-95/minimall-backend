package com.minimall.controller.api.product.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProductRegisterRequest(
        String name,
        int price,
        int stockQuantity
) {
}
