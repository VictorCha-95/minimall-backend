package com.minimall.domain.order.pay;

import com.minimall.domain.common.DomainType;
import com.minimall.domain.exception.DomainStatusException;

public class PayStatusException extends DomainStatusException {
    public PayStatusException(String message) {
        super(message);
    }

    public PayStatusException(Long payId, PayStatus currentStatus, PayStatus targetStatus) {
        super(DomainType.PAY, payId, currentStatus, targetStatus);
    }
}
