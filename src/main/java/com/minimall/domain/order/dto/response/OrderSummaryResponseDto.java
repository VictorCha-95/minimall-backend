package com.minimall.domain.order.dto.response;

import com.minimall.domain.order.OrderStatus;
import com.minimall.domain.order.dto.request.OrderItemCreateDto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderSummaryResponseDto(
        Long id,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        int itemCount,
        int finalAmount
        ) {
}
