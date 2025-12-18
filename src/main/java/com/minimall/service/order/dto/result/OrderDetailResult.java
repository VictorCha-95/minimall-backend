package com.minimall.service.order.dto.result;

import com.minimall.domain.order.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResult(
        Long id,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        int finalAmount,
        List<OrderItemResult> orderItems,
        PayResult pay,
        DeliverySummaryResult delivery
) {
}
