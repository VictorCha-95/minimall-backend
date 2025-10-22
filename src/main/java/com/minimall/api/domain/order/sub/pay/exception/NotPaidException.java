package com.minimall.api.domain.order.sub.pay.exception;

import com.minimall.api.domain.order.sub.pay.PayStatus;

public class NotPaidException extends PayStatusException {
    public NotPaidException(String message) {
        super(message);
    }
}
