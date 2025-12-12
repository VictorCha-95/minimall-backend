package com.minimall.service.order.dto;

import com.minimall.api.order.delivery.dto.DeliverySummaryResponse;
import com.minimall.api.order.dto.response.OrderItemResponse;
import com.minimall.api.order.pay.dto.PayResponse;
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
