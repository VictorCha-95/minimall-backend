package com.minimall.domain.order.sub.pay.exception;

import com.minimall.domain.DomainType;
import com.minimall.domain.order.sub.pay.PayStatus;
import com.minimall.exception.DomainStatusException;

public class PayStatusException extends DomainStatusException {
    public PayStatusException(String message) {
        super(message);
    }

    public PayStatusException(Long payId, PayStatus currentStatus, PayStatus targetStatus) {
        super(DomainType.PAY, payId, currentStatus, targetStatus);
    }
}
