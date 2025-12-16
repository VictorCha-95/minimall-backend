package com.minimall.controller.api.order.pay.dto;

import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;

import java.time.LocalDateTime;

public record PayResponse(
        PayMethod payMethod,
        int payAmount,
        PayStatus payStatus,
        LocalDateTime paidAt
) {
}
