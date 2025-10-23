package com.minimall.api.domain.product;

import com.minimall.api.domain.DomainType;
import com.minimall.api.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String fieldName, Object fieldValue) {
        super(DomainType.PRODUCT, fieldName, fieldValue);
    }
}
