package com.minimall.api.domain.order.sub.pay.exception;

import com.minimall.api.domain.DomainType;
import com.minimall.api.domain.order.sub.pay.PayStatus;
import com.minimall.api.exception.CustomStatusException;

public class PayStatusException extends CustomStatusException {
    public PayStatusException(String message) {
        super(message);
    }

    public PayStatusException(Long payId, PayStatus currentStatus, PayStatus targetStatus) {
        super(DomainType.PAY, payId, currentStatus, targetStatus);
    }
}
