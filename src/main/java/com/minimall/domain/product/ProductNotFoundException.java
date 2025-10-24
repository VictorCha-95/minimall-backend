package com.minimall.domain.product;

import com.minimall.domain.DomainType;
import com.minimall.exception.NotFoundException;

public class ProductNotFoundException extends NotFoundException {
    public ProductNotFoundException(String message) {
        super(message);
    }

    public ProductNotFoundException(String fieldName, Object fieldValue) {
        super(DomainType.PRODUCT, fieldName, fieldValue);
    }
}
