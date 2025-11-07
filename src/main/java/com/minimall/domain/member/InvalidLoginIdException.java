package com.minimall.domain.member;

import com.minimall.domain.exception.DomainExceptionMessage;
import com.minimall.domain.exception.DomainRuleException;
import lombok.Getter;

@Getter
public class InvalidLoginIdException extends DomainRuleException {

    public enum Reason { REQUIRED, BLANK, TOO_LONG }

    private static final String PARAM_NAME = "member.loginId";
    private final Reason reason;
    private final Integer length;
    private final Integer max;

    private InvalidLoginIdException(Reason reason, String message, Integer length, Integer max) {
        super(message);
        this.reason = reason;
        this.length = length;
        this.max = max;
    }

    //== factory ==//
    public static InvalidLoginIdException required() {
        return new InvalidLoginIdException(
                Reason.REQUIRED,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(PARAM_NAME),
                null, null
        );
    }

    public static InvalidLoginIdException blank() {
        return new InvalidLoginIdException(
                Reason.BLANK,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(PARAM_NAME),
                null, null
        );
    }

    public static InvalidLoginIdException tooLong(int length, int max) {
        return new InvalidLoginIdException(
                Reason.TOO_LONG,
                DomainExceptionMessage.PARAM_MAX.text(PARAM_NAME, max, length),
                length, max
        );
    }
}
