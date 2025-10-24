package com.minimall.domain.member;

import com.minimall.domain.DomainType;
import com.minimall.exception.NotFoundException;

public class MemberNotFoundException extends NotFoundException {
    public MemberNotFoundException(String message) {
        super(message);
    }

    public MemberNotFoundException(String fieldName, Object fieldValue) {
        super(DomainType.MEMBER, fieldName, fieldValue);
    }
}
