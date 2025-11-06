package com.minimall.domain.order.dto.response;

import com.minimall.domain.order.OrderStatus;

import java.time.LocalDateTime;

public record OrderCreateResponseDto(
        Long id,
        LocalDateTime orderedAt,
        OrderStatus orderStatus,
        int originalAmount,
        int discountAmount,
        int finalAmount,
        int itemCount
) {
}
