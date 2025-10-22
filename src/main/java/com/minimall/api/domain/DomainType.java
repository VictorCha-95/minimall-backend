package com.minimall.api.domain;

import lombok.Getter;

@Getter
public enum DomainType {

    MEMBER("Member"),
    PRODUCT("Product"),
    ORDER("Order"),
    PAY("Pay"),
    DELIVERY("delivery");

    private final String domainName;

    DomainType(String domainName) {
        this.domainName = domainName;
    }
}
