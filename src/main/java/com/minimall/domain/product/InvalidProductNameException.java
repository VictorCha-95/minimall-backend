package com.minimall.domain.product;

import com.minimall.domain.exception.DomainExceptionMessage;
import lombok.Getter;

@Getter
public class InvalidProductNameException extends RuntimeException {

    public enum Reason {
        REQUIRED,
        BLANK
    }

    private static final String PARAM_NAME = "product.name";
    private final Reason reason;


    private InvalidProductNameException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    //== Static Factory Methods ==//
    public static InvalidProductNameException required() {
        return new InvalidProductNameException(
                Reason.REQUIRED,
                DomainExceptionMessage.PARAM_REQUIRED_NOT_NULL.text(PARAM_NAME));
    }

    public static InvalidProductNameException blank() {
        return new InvalidProductNameException(
                Reason.BLANK,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_BLANK.text(PARAM_NAME)
        );
    }
}
