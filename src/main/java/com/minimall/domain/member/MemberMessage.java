package com.minimall.domain.member;

import lombok.Getter;

@Getter
public enum MemberMessage {

    //이름
    아몰랑("헤헤헤");

    private final String template;

    MemberMessage(String message) {
        this.template = message;
    }

    public String text(Object... args) {
        return template.formatted(args);
    }
}
