package com.minimall.service.order.dto.result;

import com.minimall.domain.order.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryResult(
        Long id,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        int itemCount,
        int finalAmount
) {
}
