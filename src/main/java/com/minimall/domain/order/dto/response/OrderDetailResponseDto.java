package com.minimall.domain.order.dto.response;

import com.minimall.domain.order.OrderStatus;
import com.minimall.domain.order.delivery.DeliverySummaryDto;
import com.minimall.domain.order.pay.PaySummaryDto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderDetailResponseDto(
        Long id,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        int finalAmount,
        List<OrderItemResponseDto> orderItems,
        PaySummaryDto pay,
        DeliverySummaryDto delivery
) {
}
