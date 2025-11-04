package com.minimall.service.exception;

import com.minimall.domain.common.DomainType;

public class MemberNotFoundException extends NotFoundException {
    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(String fieldName, Object fieldValue) {
        super(DomainType.MEMBER, fieldName, fieldValue);
    }
}
