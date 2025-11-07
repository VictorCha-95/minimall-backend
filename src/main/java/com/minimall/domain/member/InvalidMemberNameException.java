package com.minimall.domain.member;

import com.minimall.domain.exception.DomainExceptionMessage;
import com.minimall.domain.exception.DomainRuleException;
import lombok.Getter;

@Getter
public class InvalidMemberNameException extends DomainRuleException {

    public enum Reason { REQUIRED, BLANK, TOO_LONG }

    private static final String PARAM_NAME = "member.name";
    private final Reason reason;
    private final Integer length;  // TOO_LONG일 때만 의미 있음
    private final Integer max;

    private InvalidMemberNameException(Reason reason, String message, Integer length, Integer max) {
        super(message);
        this.reason = reason;
        this.length = length;
        this.max = max;
    }

    //== factory ==//
    public static InvalidMemberNameException required() {
        return new InvalidMemberNameException(
                Reason.REQUIRED,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(PARAM_NAME),
                null, null
        );
    }

    public static InvalidMemberNameException blank() {
        return new InvalidMemberNameException(
                Reason.BLANK,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(PARAM_NAME),
                null, null
        );
    }

    public static InvalidMemberNameException tooLong(int length, int max) {
        return new InvalidMemberNameException(
                Reason.TOO_LONG,
                DomainExceptionMessage.PARAM_MAX.text(PARAM_NAME, max, length),
                length, max
        );
    }
}
