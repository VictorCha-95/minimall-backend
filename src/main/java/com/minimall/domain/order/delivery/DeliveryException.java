package com.minimall.domain.order.delivery;

import com.minimall.domain.exception.DomainRuleException;

public class DeliveryException extends DomainRuleException {
    private DeliveryException(String message) {
        super(message);
    }

    public static DeliveryException notExist() {
        return new DeliveryException("Delivery must be exist");
    }
}
