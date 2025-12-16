package com.minimall.controller.api.order.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateRequest(
        @NotNull Long memberId,
        @NotNull List<OrderItemCreateRequest> items
) {}
