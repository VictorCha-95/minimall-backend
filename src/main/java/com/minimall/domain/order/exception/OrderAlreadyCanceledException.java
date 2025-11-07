package com.minimall.domain.order.exception;

import com.minimall.domain.exception.DomainRuleException;
import com.minimall.domain.order.OrderMessage;

public class OrderAlreadyCanceledException extends DomainRuleException {

    public OrderAlreadyCanceledException(Long orderId) {
        super(OrderMessage.ORDER_ALREADY_CANCELED.text(orderId));
    }
}
