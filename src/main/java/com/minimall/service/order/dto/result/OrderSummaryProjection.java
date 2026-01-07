package com.minimall.service.order.dto.result;

import com.minimall.domain.order.OrderStatus;

import java.time.LocalDateTime;

public interface OrderSummaryProjection {
    Long getId();
    LocalDateTime getOrderedAt();
    OrderStatus getOrderStatus();
    Long getItemCount();
    Integer getFinalAmount();
}
