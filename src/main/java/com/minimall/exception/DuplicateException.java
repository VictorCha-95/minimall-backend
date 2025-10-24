package com.minimall.exception;

public class DuplicateException extends RuntimeException {
    public DuplicateException(String message) {
        super(message);
    }

    public static DuplicateException ofField(String fieldName, Object fieldValue) {
        return new DuplicateException(String.format("중복되는 %s은 사용할 수 없습니다. (이미 존재하는 값: %s)",
                fieldName, fieldValue));
    }
}
