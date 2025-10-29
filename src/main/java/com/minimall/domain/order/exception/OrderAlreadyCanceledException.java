package com.minimall.domain.order.exception;

public class OrderAlreadyCanceledException extends RuntimeException {
    public OrderAlreadyCanceledException(String message) {
        super(message);
    }

    public OrderAlreadyCanceledException(Long orderId) {
        super("이미 취소된 주문 - orderId: " + orderId + ", Current: CANCELED");
    }


}
