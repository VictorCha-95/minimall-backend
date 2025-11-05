package com.minimall.domain.product;

public enum ProductMessage {

    //재고
    STOCK_INSUFFICIENT("재고 수량이 부족합니다. (재고 수량: %d, 요청값: %d)");

    private final String template;

    ProductMessage(String message) {
        this.template = message;
    }

    public String text(Object... args) {
        return template.formatted(args);
    }
}
