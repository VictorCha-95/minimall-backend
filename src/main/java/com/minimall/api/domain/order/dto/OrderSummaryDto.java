package com.minimall.api.domain.order.dto;

import java.time.LocalDateTime;

public record OrderSummaryDto(
        Long orderId,
        LocalDateTime orderDate,
        int totalAmount
) {
}
