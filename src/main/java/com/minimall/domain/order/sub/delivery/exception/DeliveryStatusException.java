package com.minimall.domain.order.sub.delivery.exception;

import com.minimall.domain.DomainType;
import com.minimall.domain.order.sub.delivery.DeliveryStatus;
import com.minimall.exception.DomainStatusException;

public class DeliveryStatusException extends DomainStatusException {
    public DeliveryStatusException(String message) {
        super(message);
    }

    public DeliveryStatusException(Long deliveryId, DeliveryStatus currentStatus, DeliveryStatus targetStatus) {
        super(DomainType.DELIVERY, deliveryId, currentStatus, targetStatus);
    }


}
