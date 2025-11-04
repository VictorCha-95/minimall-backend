package com.minimall.domain.order.delivery;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.exception.DomainStatusException;

public class DeliveryStatusException extends DomainStatusException {
    public DeliveryStatusException(String message) {
        super(message);
    }

    public DeliveryStatusException(Long deliveryId, DeliveryStatus currentStatus, DeliveryStatus targetStatus) {
        super(DomainType.DELIVERY, deliveryId, currentStatus, targetStatus);
    }


}
