package com.minimall.api.exception;

import com.minimall.api.domain.DomainType;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(DomainType target, String fieldName, Object fieldValue) {
        super(String.format("%s을(를) 찾을 수 없습니다. (%s: %s)", target.getDisPlayName(), fieldName, fieldValue));
    }
}
