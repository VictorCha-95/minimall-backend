package com.minimall.api.order.dto;

import java.time.LocalDateTime;

public record OrderSummaryDto(
        Long id,
        LocalDateTime orderedAt,
        int totalAmount
) {
}
