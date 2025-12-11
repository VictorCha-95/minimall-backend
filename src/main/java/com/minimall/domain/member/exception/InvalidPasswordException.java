package com.minimall.domain.member.exception;

import com.minimall.domain.exception.DomainExceptionMessage;
import com.minimall.domain.exception.DomainRuleException;
import lombok.Getter;

@Getter
public class InvalidPasswordException extends DomainRuleException {

    public enum Reason { REQUIRED, BLANK, TOO_SHORT, TOO_LONG }

    private static final String PARAM_NAME = "member.password";
    private final Reason reason;
    private final Integer length;
    private final Integer limit;

    private InvalidPasswordException(Reason reason, String message, Integer length, Integer limit) {
        super(message);
        this.reason = reason;
        this.length = length;
        this.limit = limit;
    }

    //== factory ==//
    public static InvalidPasswordException required() {
        return new InvalidPasswordException(
                Reason.REQUIRED,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(PARAM_NAME),
                null, null
        );
    }

    public static InvalidPasswordException blank() {
        return new InvalidPasswordException(
                Reason.BLANK,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(PARAM_NAME),
                null, null
        );
    }

    public static InvalidPasswordException tooShort(int length, int min) {
        return new InvalidPasswordException(
                Reason.TOO_SHORT,
                DomainExceptionMessage.PARAM_MIN.text(PARAM_NAME, min, length),
                length, min
        );
    }

    public static InvalidPasswordException tooLong(int length, int max) {
        return new InvalidPasswordException(
                Reason.TOO_LONG,
                DomainExceptionMessage.PARAM_MAX.text(PARAM_NAME, max, length),
                length, max
        );
    }
}
