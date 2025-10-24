package com.minimall.domain;

import lombok.Getter;

@Getter
public enum DomainType {

    MEMBER("회원"),
    PRODUCT("상품"),
    ORDER("주문"),
    ORDER_ITEM("주문 항목"),
    PAY("결제"),
    DELIVERY("배송");

    private final String disPlayName;

    DomainType(String domainName) {
        this.disPlayName = domainName;
    }
}
