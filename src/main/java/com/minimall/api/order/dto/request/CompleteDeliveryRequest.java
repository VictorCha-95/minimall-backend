package com.minimall.controller.api.order.dto.request;

import jakarta.validation.constraints.Null;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;

public record CompleteDeliveryRequest(
        @Nullable LocalDateTime arrivedAt
        ) {
}
