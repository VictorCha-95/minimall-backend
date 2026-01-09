package com.minimall.service.order.dto.result;

import com.minimall.domain.order.OrderStatus;

import java.time.LocalDateTime;

public record OrderCreateResult(
        Long id,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        int originalAmount,
        int discountAmount,
        int finalAmount,
        int itemCount
) {
}
