package com.minimall.api.order.dto.response;

import com.minimall.domain.order.OrderStatus;
import com.minimall.api.order.delivery.dto.DeliverySummaryResponse;
import com.minimall.api.order.pay.dto.PayResponse;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponse(
        Long id,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        int finalAmount,
        List<OrderItemResponse> orderItems,
        PayResponse pay,
        DeliverySummaryResponse delivery
) {
}
