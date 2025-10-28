package com.minimall.exception;

public class DuplicateException extends RuntimeException {

    public DuplicateException(String message) {
        super(message);
    }

    public static void validateField(String fieldName, Object fieldValue) {
        if (fieldValue != null) {
            throw new DuplicateException(
                    String.format("중복되는 %s는(은) 사용할 수 없습니다. (이미 존재하는 값: %s)", fieldName, fieldValue));
        }
    }
}
