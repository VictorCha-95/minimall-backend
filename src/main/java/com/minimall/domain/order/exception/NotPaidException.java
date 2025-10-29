package com.minimall.domain.order.exception;

public class NotPaidException extends PayStatusException {
    public NotPaidException(String message) {
        super(message);
    }
}
