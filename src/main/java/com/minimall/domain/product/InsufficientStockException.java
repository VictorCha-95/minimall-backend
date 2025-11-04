package com.minimall.domain.product;

public class InsufficientStockException extends RuntimeException {

    public static InsufficientStockException ofStockQuantity(int stockQuantity) {
        return new InsufficientStockException(ProductMessage.STOCK_INSUFFICIENT.text(stockQuantity));
    }

    private InsufficientStockException(String message) {
        super(message);
    }
}
