package com.minimall.domain.order.exception;

import com.minimall.domain.order.OrderMessage;
import com.minimall.domain.order.pay.PayStatus;

public class PaymentRequiredException extends RuntimeException {
    public static PaymentRequiredException mustBePaidBeforeDelivery(Long orderId) {
        return new PaymentRequiredException(
                OrderMessage.PAYMENT_REQUIRED_FOR_DELIVERY_PREPARE.text(orderId));
    }

    public static PaymentRequiredException mustBePaidBeforeDelivery(Long orderId, PayStatus payStatus) {
        return new PaymentRequiredException(
                OrderMessage.PAYMENT_REQUIRED_FOR_DELIVERY_PREPARE_WITH_STATUS.text(orderId, payStatus));
    }

    private PaymentRequiredException(String message) {
        super(message);
    }
}
