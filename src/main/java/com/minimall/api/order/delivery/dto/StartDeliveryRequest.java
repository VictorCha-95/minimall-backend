package com.minimall.api.order.delivery.dto;

import jakarta.validation.constraints.NotBlank;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public record StartDeliveryRequest(
        @NotBlank String trackingNo,
        @Nullable LocalDateTime shippedAt
) {
}
