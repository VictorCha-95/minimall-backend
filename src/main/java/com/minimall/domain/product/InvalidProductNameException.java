package com.minimall.domain.product;

public class InvalidProductNameException extends RuntimeException {
    public InvalidProductNameException(String message) {
        super(message);
    }

    public static InvalidProductNameException empty() {
        return new InvalidProductNameException("상품명은 필수 입력값입니다.");
    }
}
