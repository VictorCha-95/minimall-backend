package com.minimall.domain.order.pay;

import java.time.LocalDateTime;

public record PaySummaryDto(
        Long id,
        PayMethod payMethod,
        PayStatus payStatus,
        LocalDateTime paidAt
) {
}
