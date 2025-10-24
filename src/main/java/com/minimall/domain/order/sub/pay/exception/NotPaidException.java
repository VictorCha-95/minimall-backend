package com.minimall.domain.order.sub.pay.exception;

public class NotPaidException extends PayStatusException {
    public NotPaidException(String message) {
        super(message);
    }
}
