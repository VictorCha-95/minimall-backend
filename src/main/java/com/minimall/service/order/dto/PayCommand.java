package com.minimall.service.order.dto;

import com.minimall.domain.order.pay.PayMethod;
import jakarta.validation.constraints.NotNull;

public record PayCommand(
        @NotNull PayMethod payMethod,
        @NotNull int payAmount
) {}
