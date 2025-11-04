package com.minimall.domain.order.status;

import com.minimall.domain.common.CustomStatus;

import java.util.Map;
import java.util.Set;

public enum OrderStatus implements CustomStatus {

    ORDERED,        // 주문 생성 직후(결제 전)
    CONFIRMED,      // 결제 완료 후 주문 확정 상태
    SHIP_READY,
    SHIPPING,
    COMPLETED,       // 결제/배송 모두 끝난 상태
    CANCELED;

    private static final Map<OrderStatus, Set<OrderStatus>> transitions = Map.of(
        ORDERED, Set.of(CONFIRMED, CANCELED),
        CONFIRMED, Set.of(SHIP_READY, CANCELED),
        SHIP_READY, Set.of(SHIPPING, CANCELED),
        SHIPPING, Set.of(COMPLETED),
        COMPLETED, Set.of(),
        CANCELED, Set.of()
    );

    public boolean canProgressTo(OrderStatus next) {
        return transitions.getOrDefault(this, Set.of()).contains(next);
    }
}