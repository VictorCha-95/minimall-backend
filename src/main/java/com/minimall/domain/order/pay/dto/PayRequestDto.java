package com.minimall.domain.order.pay.dto;

import com.minimall.domain.order.pay.PayMethod;
import jakarta.validation.constraints.NotNull;

public record PayRequestDto(
        @NotNull PayMethod payMethod,
        @NotNull int payAmount
) {
}
