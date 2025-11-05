package com.minimall.domain.product;

import com.minimall.domain.exception.DomainExceptionMessage;
import lombok.Getter;

@Getter
public class InvalidPriceException extends RuntimeException {

    public enum Reason {
        NEGATIVE,
        REQUIRED
    }

    private static final String PARAM_NAME = "product.price";
    private final Reason reason;
    private final Integer requested;

    private InvalidPriceException(Reason reason, String message, Integer requested) {
        super(message);
        this.reason = reason;
        this.requested = requested;
    }

    //== Static Factory Methods ==//
    public static InvalidPriceException negative(int requested) {
        return new InvalidPriceException(
                Reason.NEGATIVE,
                DomainExceptionMessage.PARAM_CANNOT_BE_NEGATIVE.text(PARAM_NAME, requested),
                requested);
    }

    public static InvalidPriceException empty() {
        return new InvalidPriceException(
                Reason.REQUIRED,
                DomainExceptionMessage.PARAM_REQUIRED_NOT_NULL.text(PARAM_NAME),
                null);
    }
}
