package com.minimall.domain.product;

public class InvalidStockQuantityException extends RuntimeException {
    public InvalidStockQuantityException(String message) {
        super(message);
    }

    public static InvalidStockQuantityException cannotBeNegative(int stockQuantity) {
        return new InvalidStockQuantityException("재고 수량은 음수일 수 없습니다. 요청하신 재고 수량: " + stockQuantity);
    }

    public static InvalidStockQuantityException cannotBeNull() {
        return new InvalidStockQuantityException("재고 수량은 필수 입력값입니다.");
    }
}
