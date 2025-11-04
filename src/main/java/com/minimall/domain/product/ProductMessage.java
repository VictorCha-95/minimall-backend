package com.minimall.domain.product;

public enum ProductMessage {

    //재고
    STOCK_INSUFFICIENT("재고 수량이 부족합니다. 현재 재고 수량: %d, 요청하신 재고 감량 수량: %d"),
    STOCK_CANNOT_REQUIRE_NEGATIVE("재고 수량은 음수일 수 없습니다. 요청하신 재고 수량: %d"),
    STOCK_REQUIRED("재고 수량은 필수 입력값입니다."),

    //가격
    PRICE_CANNOT_BE_NEGATIVE("가격은 음수일 수 없습니다. 요청하신 가격: %d"),
    PRICE_REQUIRED("가격은 필수 입력 값입니다."),

    //이름
    NAME_REQUIRED("상품명은 필수 입력값입니다.");





    private final String template;

    ProductMessage(String message) {
        this.template = message;
    }

    public String text(Object... args) {
        return template.formatted(args);
    }
}
