package com.minimall.domain.order.exception;

import com.minimall.domain.order.OrderMessage;

public class OrderAlreadyCanceledException extends RuntimeException {

    public OrderAlreadyCanceledException(Long orderId) {
        super(OrderMessage.ORDER_ALREADY_CANCELED.text(orderId));
    }
}
