package com.minimall.domain.order.discount;

import com.minimall.domain.exception.DomainRuleException;

public class DiscountException extends DomainRuleException {
    public DiscountException(String message) {
        super(message);
    }

    public static DiscountException alreadyDiscounted() {
        return new DiscountException("이미 할인이 적용되었습니다.");
    }

    public static DiscountException discountAmountGreaterThanOrderAmount(int originalAmount, int discountAmount) {
        return new DiscountException(
                String.format("할인 금액이 총 금액 보다 많습니다. 총 금액: %d, 할인 금액: %d", originalAmount, discountAmount));
    }

}
