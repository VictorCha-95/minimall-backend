package com.minimall.service.exception;

import com.minimall.domain.common.DomainType;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(String message) {
        super(message);
    }

    public OrderNotFoundException(String fieldName, Object fieldValue) {
        super(DomainType.ORDER, fieldName, fieldValue);
    }
}
