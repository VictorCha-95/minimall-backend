package com.minimall.domain.order.exception;

public class InvalidOrderQuantityException extends RuntimeException {
    public InvalidOrderQuantityException(String message) {
        super(message);
    }

    public static InvalidOrderQuantityException mustBeGreaterThanZero(int orderQuantity) {
        return new InvalidOrderQuantityException("주문 항목의 주문 수량은 0보다 커야 합니다. 요청하신 주문 수량: " + orderQuantity);
    }
}
