package com.minimall.domain.order.delivery;

import com.minimall.domain.common.CustomStatus;

import java.util.Map;
import java.util.Set;

public enum DeliveryStatus implements CustomStatus {
    READY,
    SHIPPING,
    COMPLETED,
    FAILED,
    CANCELED;

    private final static Map<DeliveryStatus, Set<DeliveryStatus>> transitions = Map.of(
            READY, Set.of(SHIPPING, FAILED, CANCELED),
            SHIPPING, Set.of(COMPLETED, FAILED),
            COMPLETED, Set.of(),
            FAILED, Set.of(),
            CANCELED, Set.of()
    );

    public boolean canProgressTo(DeliveryStatus next) {
        return transitions.getOrDefault(this, Set.of()).contains(next);
    }
}
