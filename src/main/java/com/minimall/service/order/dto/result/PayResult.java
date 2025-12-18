package com.minimall.service.order.dto.result;

import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;

import java.time.LocalDateTime;

public record PayResult(
        PayMethod payMethod,
        int payAmount,
        PayStatus payStatus,
        LocalDateTime paidAt
) {
}
