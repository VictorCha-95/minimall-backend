package com.minimall.domain.order.dto.request;

import com.minimall.domain.order.OrderItem;

import java.util.List;

public record OrderCreateRequestDto(
        Long memberId,
        List<OrderItemCreateDto> items
) {}
