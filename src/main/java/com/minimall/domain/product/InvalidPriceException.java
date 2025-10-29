package com.minimall.domain.product;

public class InvalidPriceException extends RuntimeException {
    public InvalidPriceException(String message) {
        super(message);
    }

    public static InvalidPriceException priceCannotBeNegative(int price) {
        return new InvalidPriceException("가격은 음수일 수 없습니다. 요청하신 가격: " + price);
    }

    public static InvalidPriceException priceCannotBeNull() {
        return new InvalidPriceException("가격은 필수 입력 값입니다.");
    }
}
