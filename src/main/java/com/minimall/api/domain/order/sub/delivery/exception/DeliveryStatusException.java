package com.minimall.api.domain.order.sub.delivery.exception;

import com.minimall.api.domain.order.sub.delivery.DeliveryStatus;

public class DeliveryStatusException extends RuntimeException {
    public DeliveryStatusException(String message) {
        super(message);
    }

    public DeliveryStatusException(Long orderId, DeliveryStatus currentStatus, DeliveryStatus requireStatus) {
        super(String.format("Delivery 상태 오류 - orderId: %d, Current: %s, Require: %s," ,
                orderId, currentStatus, requireStatus));
    }


}
