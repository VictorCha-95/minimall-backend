package com.minimall.controller.api.order.pay.dto;

import com.minimall.domain.order.pay.PayMethod;
import jakarta.validation.constraints.NotNull;

public record PayRequest(
        @NotNull PayMethod payMethod,
        @NotNull int payAmount
) {
}
