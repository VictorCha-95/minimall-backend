package com.minimall.api.domain.order.exception;

import com.minimall.api.domain.DomainType;
import com.minimall.api.domain.order.OrderStatus;
import com.minimall.api.exception.CustomStatusException;

public class OrderStatusException extends CustomStatusException {
    public OrderStatusException(String message) {
        super(message);
    }

    public OrderStatusException(Long orderId, OrderStatus currentStatus, OrderStatus targetStatus) {
        super(DomainType.ORDER, orderId, currentStatus, targetStatus);
    }
}
