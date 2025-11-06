package com.minimall.domain.order.dto.request;

import com.minimall.domain.order.OrderItem;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequestDto(
        @NotNull Long memberId,
        @NotNull List<OrderItemCreateDto> items
) {}
