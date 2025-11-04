package com.minimall.domain.product;

import lombok.Getter;

@Getter
public class InvalidProductStockException extends RuntimeException {

    public enum Reason {
        NEGATIVE,       //음수 재고 요청
        NULL_REQUIRED,  //재고 null 금지
        INSUFFICIENT    //재고 부족
    }

    private final Reason reason;
    private final Integer requested;
    private final Integer available;

    private InvalidProductStockException(Reason reason, String message, Integer requested, Integer available) {
        super(message);
        this.reason = reason;
        this.requested = requested;
        this.available = available;
    }


    //== Static Factory ==//
    public static InvalidProductStockException cannotBeNegative(int requested) {
        return new InvalidProductStockException(
                Reason.NEGATIVE,
                ProductMessage.STOCK_CANNOT_REQUIRE_NEGATIVE.text(requested),
                requested, null);
    }

    public static InvalidProductStockException cannotBeNull() {
        return new InvalidProductStockException(
                Reason.NULL_REQUIRED,
                ProductMessage.STOCK_REQUIRED.text(),
                null, null);
    }

    public static InvalidProductStockException insufficient(int requested, int available) {
        return new InvalidProductStockException(
                Reason.INSUFFICIENT,
                ProductMessage.STOCK_INSUFFICIENT.text(requested, available),
                requested, available
        );
    }
}