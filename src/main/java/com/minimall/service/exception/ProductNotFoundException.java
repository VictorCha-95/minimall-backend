package com.minimall.service.exception;

import com.minimall.domain.common.DomainType;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String fieldName, Object fieldValue) {
        super(DomainType.PRODUCT, fieldName, fieldValue);
    }
}
