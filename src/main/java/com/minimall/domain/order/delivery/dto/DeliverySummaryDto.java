package com.minimall.domain.order.delivery.dto;

import com.minimall.domain.embeddable.AddressDto;
import com.minimall.domain.order.delivery.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliverySummaryDto(
        DeliveryStatus deliveryStatus,
        String trackingNo,
        AddressDto shipAddr,
        LocalDateTime shippedAt,
        LocalDateTime arrivedAt
) {
}
