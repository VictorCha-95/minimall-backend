package com.minimall.domain.exception;

public enum DomainExceptionMessage {

    //상태
    STATUS_ERROR("[%s - id: %d] 상태 오류  Current Status: %s, Try Status: %s"),

    //중복
    DUPLICATE_FIELD("중복되는 %s는(은) 사용할 수 없습니다. (이미 존재하는 값: %s)");

    private final String template;

    DomainExceptionMessage(String message) {
        this.template = message;
    }

    public String text(Object... args) {
        return template.formatted(args);
    }
}
