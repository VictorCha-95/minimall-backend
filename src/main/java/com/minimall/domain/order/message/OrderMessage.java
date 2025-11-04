package com.minimall.domain.order.message;

public enum OrderMessage {

    // 생성
    EMPTY_ORDER_ITEM("주문 생성 시 주문 항목이 반드시 1개 이상 포함되어야 하며, 주문 항목은 null 값을 허용하지 않습니다."),
    INVALID_ORDER_QUANTITY("주문 항목의 주문 수량은 0보다 커야 합니다. 요청하신 주문 수량: %d"),
    MEMBER_REQUIRED_FOR_ORDER_CREATION("주문 생성 시 회원 정보는 필수입니다."),

    // 취소
    ORDER_ALREADY_CANCELED("이미 취소된 주문 - orderId: %d, Current: CANCELED");

    //


    private final String template;

    OrderMessage(String message) {
        this.template = message;
    }

    public String text(Object... args) {
        return template.formatted(args);
    }
}
