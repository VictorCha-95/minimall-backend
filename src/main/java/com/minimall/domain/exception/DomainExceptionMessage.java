package com.minimall.domain.exception;

public enum DomainExceptionMessage {

    // 상태
    STATUS_ERROR("[%s - id: %d] 상태 오류: current=%s, try=%s"),

    // 중복
    DUPLICATE_PARAM("'%s'는(은) 이미 사용 중입니다. (값: '%s')"),

    // 필수/공백
    PARAM_REQUIRE_NOT_NULL("'%s'는(은) 필수입니다."),
    PARAM_REQUIRE_NOT_BLANK("'%s'는(은) 공백일 수 없습니다."),

    // 음수/양수
    PARAM_CANNOT_BE_NEGATIVE("'%s'는(은) 음수가 될 수 없습니다. (값: %d)"),
    PARAM_REQUIRE_POSITIVE("'%s'는(은) 0보다 커야 합니다. (값: %d)"),

    // 범위/형식
    PARAM_OUT_OF_RANGE("'%s' 값은 %d~%d 범위여야 합니다. (값: %d)"),
    PARAM_MIN("'%s'는(은) %d 이상이어야 합니다. (값: %d)"),
    PARAM_MAX("'%s'는(은) %d 이하여야 합니다. (값: %d)"),
    PARAM_INVALID_FORMAT("'%s' 형식이 올바르지 않습니다. (값: '%s')");


    private final String template;

    DomainExceptionMessage(String message) {
        this.template = message;
    }

    public String text(Object... args) {
        return template.formatted(args);
    }
}
