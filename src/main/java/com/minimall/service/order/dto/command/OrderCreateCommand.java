package com.minimall.service.order.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateCommand(
        @NotNull Long memberId,
        @NotNull List<OrderItemCreateCommand> items
) {
}
