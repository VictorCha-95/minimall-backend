package com.minimall.domain.embeddable;

import com.minimall.domain.exception.DomainExceptionMessage;
import lombok.Getter;

@Getter
public class InvalidAddressException extends RuntimeException {

    public enum Reason {
        REQUIRED,
        EMPTY_REQUIRED_FIELDS
    }

    private final Reason reason;
    private static final String PARAM_NAME = "addr";


    private InvalidAddressException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public static InvalidAddressException required() {
        return new InvalidAddressException(
                Reason.REQUIRED,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(PARAM_NAME));
    }

    public static InvalidAddressException missingRequiredFields() {
        return new InvalidAddressException(
                Reason.EMPTY_REQUIRED_FIELDS,
                "주소의 필수 항목이 누락되었습니다.");
    }


}
