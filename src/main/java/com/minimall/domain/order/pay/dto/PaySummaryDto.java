package com.minimall.domain.order.pay.dto;

import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;

import java.time.LocalDateTime;

public record PaySummaryDto(
        PayMethod payMethod,
        int payAmount,
        PayStatus payStatus,
        LocalDateTime paidAt
) {
}
