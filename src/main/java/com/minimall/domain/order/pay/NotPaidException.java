package com.minimall.domain.order.pay;

public class NotPaidException extends PayStatusException {
    public NotPaidException(String message) {
        super(message);
    }
}
