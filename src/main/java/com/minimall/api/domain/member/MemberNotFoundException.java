package com.minimall.api.domain.member;

import com.minimall.api.domain.DomainType;
import com.minimall.api.exception.NotFoundException;

public class MemberNotFoundException extends NotFoundException {
    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(String fieldName, Object fieldValue) {
        super(DomainType.MEMBER, fieldName, fieldValue);
    }
}
