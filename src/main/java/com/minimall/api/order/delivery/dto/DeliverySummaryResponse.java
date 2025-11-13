package com.minimall.api.order.delivery.dto;

import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.domain.order.delivery.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliverySummaryResponse(
        DeliveryStatus deliveryStatus,
        String trackingNo,
        AddressDto shipAddr,
        LocalDateTime shippedAt,
        LocalDateTime arrivedAt
) {
}
