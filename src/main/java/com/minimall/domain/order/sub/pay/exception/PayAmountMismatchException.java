package com.minimall.domain.order.sub.pay.exception;

public class PayAmountMismatchException extends RuntimeException {
    public PayAmountMismatchException(String message) {
        super(message);
    }

    public PayAmountMismatchException(Long orderId, int orderAmount, int payAmount) {
        super(String.format("결제 금액 불일치 오류 - orderId: %d, orderAmount: %d, payAmount: %d", orderId, orderAmount, payAmount));
    }
}
