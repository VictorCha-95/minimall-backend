package com.minimall.api.domain.order.sub.pay;

import com.minimall.api.common.CustomStatus;

import java.util.Map;
import java.util.Set;

public enum PayStatus implements CustomStatus {
    READY,
    PAID,
    FAILED,
    CANCELED;
    //TODO PROCESSING, REFUND 과정 추가


    private final static Map<PayStatus, Set<PayStatus>> transitions = Map.of(
        READY, Set.of(PAID, CANCELED, FAILED),
        PAID, Set.of(CANCELED),
        FAILED, Set.of(READY),
        CANCELED, Set.of()
    );

    public boolean canProgressTo(PayStatus next) {
        return transitions.getOrDefault(this, Set.of()).contains(next);
    }
}


