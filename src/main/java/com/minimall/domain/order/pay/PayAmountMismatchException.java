package com.minimall.domain.order.pay;

import com.minimall.domain.exception.DomainRuleException;

public class PayAmountMismatchException extends DomainRuleException {

    public PayAmountMismatchException(Long orderId, int orderAmount, int payAmount) {
        super(String.format("결제 금액 불일치 오류 - orderId: %d, orderAmount: %d, payAmount: %d", orderId, orderAmount, payAmount));
    }
}
