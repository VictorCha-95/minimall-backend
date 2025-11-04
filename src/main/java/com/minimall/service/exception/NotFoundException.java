package com.minimall.service.exception;

import com.minimall.domain.common.DomainType;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(DomainType target, String fieldName, Object fieldValue) {
        super(String.format("%s을(를) 찾을 수 없습니다. (%s: %s)", target.getDisPlayName(), fieldName, fieldValue));
    }
}
