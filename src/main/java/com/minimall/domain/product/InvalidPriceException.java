package com.minimall.domain.product;

public class InvalidPriceException extends RuntimeException {
    public static InvalidPriceException priceCannotBeNegative(int price) {
        return new InvalidPriceException(ProductMessage.PRICE_CANNOT_BE_NEGATIVE.text(price));
    }

    public static InvalidPriceException priceCannotBeNull() {
        return new InvalidPriceException(ProductMessage.PRICE_REQUIRED.text());
    }

    private InvalidPriceException(String message) {
        super(message);
    }
}
