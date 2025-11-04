package com.minimall.domain.product;

public class InvalidProductNameException extends RuntimeException {
    public static InvalidProductNameException empty() {
        return new InvalidProductNameException(ProductMessage.NAME_REQUIRED.text());
    }

    private InvalidProductNameException(String message) {
        super(message);
    }
}
