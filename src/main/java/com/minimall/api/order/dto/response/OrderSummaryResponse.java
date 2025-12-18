package com.minimall.api.order.dto.response;

import com.minimall.domain.order.OrderStatus;

import java.time.LocalDateTime;

public record OrderSummaryResponse(
        Long id,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        int itemCount,
        int finalAmount
        ) {
}
