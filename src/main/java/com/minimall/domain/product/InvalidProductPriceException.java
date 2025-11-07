package com.minimall.domain.product;

import com.minimall.domain.exception.DomainExceptionMessage;
import com.minimall.domain.exception.DomainRuleException;
import lombok.Getter;

@Getter
public class InvalidProductPriceException extends DomainRuleException {

    public enum Reason {
        NEGATIVE,
        REQUIRED
    }

    private static final String PARAM_NAME = "product.price";
    private final Reason reason;
    private final Integer requested;

    private InvalidProductPriceException(Reason reason, String message, Integer requested) {
        super(message);
        this.reason = reason;
        this.requested = requested;
    }

    //== Static Factory Methods ==//
    public static InvalidProductPriceException negative(int requested) {
        return new InvalidProductPriceException(
                Reason.NEGATIVE,
                DomainExceptionMessage.PARAM_CANNOT_BE_NEGATIVE.text(PARAM_NAME, requested),
                requested);
    }

    public static InvalidProductPriceException required() {
        return new InvalidProductPriceException(
                Reason.REQUIRED,
                DomainExceptionMessage.PARAM_REQUIRE_NOT_NULL.text(PARAM_NAME),
                null);
    }
}
