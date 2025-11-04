package com.minimall.domain.order.exception;

import com.minimall.domain.order.message.OrderMessage;

public class EmptyOrderItemException extends RuntimeException {

    public EmptyOrderItemException() {
        super(OrderMessage.EMPTY_ORDER_ITEM.text());
    }
}
