package com.minimall.domain.exception;

public class DuplicateException extends RuntimeException {

    public static void validateField(String fieldName, Object fieldValue) {
        throw new DuplicateException(DomainExceptionMessage.DUPLICATE_PARAM.text(fieldName, fieldValue));
    }

    private DuplicateException(String message) {
        super(message);
    }
}
