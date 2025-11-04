package com.minimall.domain.order.exception;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.order.status.OrderStatus;
import com.minimall.domain.exception.DomainStatusException;

public class OrderStatusException extends DomainStatusException {

    public OrderStatusException(Long orderId, OrderStatus currentStatus, OrderStatus targetStatus) {
        super(DomainType.ORDER, orderId, currentStatus, targetStatus);
    }
}
