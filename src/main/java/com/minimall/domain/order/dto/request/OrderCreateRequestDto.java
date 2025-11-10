package com.minimall.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequestDto(
        @NotNull Long memberId,
        @NotNull List<OrderItemCreateDto> items
) {}
