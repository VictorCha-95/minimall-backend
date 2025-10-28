package com.minimall.domain.order;

import com.minimall.domain.DomainType;
import com.minimall.exception.DomainStatusException;

public class OrderStatusException extends DomainStatusException {
    public OrderStatusException(String message) {
        super(message);
    }

    public OrderStatusException(Long orderId, OrderStatus currentStatus, OrderStatus targetStatus) {
        super(DomainType.ORDER, orderId, currentStatus, targetStatus);
    }
}
