package com.minimall.domain.order.delivery;

import com.minimall.domain.embeddable.AddressDto;

import java.time.LocalDateTime;

public record DeliverySummaryDto(
        DeliveryStatus deliveryStatus,
        String trackingNo,
        AddressDto shipAddr,
        LocalDateTime shippedAt,
        LocalDateTime arrivedAt
) {
}
