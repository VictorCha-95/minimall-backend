package com.minimall.domain.order.exception;

import com.minimall.domain.order.message.OrderMessage;

public class InvalidOrderQuantityException extends RuntimeException {

    public static InvalidOrderQuantityException mustBeGreaterThanZero(int orderQuantity) {
        return new InvalidOrderQuantityException(OrderMessage.INVALID_ORDER_QUANTITY.text(orderQuantity));
    }

    private InvalidOrderQuantityException(String message) {
        super(message);
    }

}
