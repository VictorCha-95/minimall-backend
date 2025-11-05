package com.minimall.domain.member;

import com.minimall.domain.exception.DomainExceptionMessage;
import lombok.Getter;

@Getter
public class InvalidEmailException extends RuntimeException {

    public enum Reason { REQUIRED, BLANK, FORMAT }

    private static final String PARAM_NAME = "member.email";
    private final Reason reason;
    private final String value;

    private InvalidEmailException(Reason reason, String message, String value) {
        super(message);
        this.reason = reason;
        this.value = value;
    }

    //== factory ==//
    public static InvalidEmailException required() {
        return new InvalidEmailException(
                Reason.REQUIRED,
                DomainExceptionMessage.PARAM_REQUIRED_NOT_NULL.text(PARAM_NAME),
                null
        );
    }

    public static InvalidEmailException blank() {
        return new InvalidEmailException(
                Reason.BLANK,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(PARAM_NAME),
                null
        );
    }

    public static InvalidEmailException format(String value) {
        return new InvalidEmailException(
                Reason.FORMAT,
                DomainExceptionMessage.PARAM_INVALID_FORMAT.text(PARAM_NAME, value),
                value
        );
    }
}
