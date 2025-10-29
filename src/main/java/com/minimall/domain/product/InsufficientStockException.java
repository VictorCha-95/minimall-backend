package com.minimall.domain.product;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String message) {
        super(message);
    }

  public static InsufficientStockException ofStockQuantity(int stockQuantity) {
    return new InsufficientStockException("재고 수량이 부족합니다. 현재 재고: " + stockQuantity);
  }
}
