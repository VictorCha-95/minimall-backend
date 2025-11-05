package com.minimall.domain.order;

public enum OrderMessage {

    // 생성
    REQUIRE_ORDER_ITEM("주문 생성 시 주문 항목이 1개 이상이어야 합니다."),
    NULL_ORDER_ITEM_AT("주문 항목에 null이 포함되었습니다. index: %d"),
    INVALID_ORDER_QUANTITY("주문 수량은 0보다 커야 합니다. 요청 수량: %d"),
    MEMBER_REQUIRED_FOR_ORDER_CREATION("주문 생성 시 회원 정보는 필수입니다."),

    // 취소
    ORDER_ALREADY_CANCELED("이미 취소된 주문 - orderId: %d, Current: CANCELED"),

    // 배송
    PAYMENT_REQUIRED_FOR_DELIVERY_PREPARE("결제가 완료되지 않아 배송을 준비할 수 없습니다. orderId: %d"),
    PAYMENT_REQUIRED_FOR_DELIVERY_PREPARE_WITH_STATUS("결제가 완료되지 않아 배송을 준비할 수 없습니다. orderId: %d, payStatus: %s");


    private final String template;

    OrderMessage(String message) {
        this.template = message;
    }

    public String text(Object... args) {
        return template.formatted(args);
    }
}
