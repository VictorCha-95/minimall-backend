package com.minimall.api.domain.order.sub.pay.exception;

import com.minimall.api.domain.order.sub.delivery.DeliveryStatus;
import com.minimall.api.domain.order.sub.pay.PayStatus;

public class PayStatusException extends RuntimeException {
    public PayStatusException(String message) {
        super(message);
    }

    public PayStatusException(Long orderId, PayStatus currentStatus, String action) {
        super(String.format("Pay 상태 오류 - orderId: %d, Action: %s, Current: %s",
                orderId, action, currentStatus));
    }
}
