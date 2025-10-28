package com.minimall.domain.order.sub.pay;

public class NotPaidException extends PayStatusException {
    public NotPaidException(String message) {
        super(message);
    }
}
