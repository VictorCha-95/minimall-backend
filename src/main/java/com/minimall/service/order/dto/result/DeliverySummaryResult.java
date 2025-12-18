package com.minimall.service.order.dto.result;

import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.domain.order.delivery.DeliveryStatus;

import java.time.LocalDateTime;

public record DeliverySummaryResult(
        DeliveryStatus deliveryStatus,
        String trackingNo,
        AddressDto shipAddr,
        LocalDateTime shippedAt,
        LocalDateTime arrivedAt
) {
}
