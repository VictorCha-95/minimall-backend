package com.minimall.fixture;

import com.minimall.api.order.pay.dto.PayRequest;
import com.minimall.api.order.pay.dto.PayResponse;
import com.minimall.domain.order.Pay;
import com.minimall.domain.order.pay.PayMethod;
import com.minimall.domain.order.pay.PayStatus;
import com.minimall.service.order.dto.command.PayCommand;

import java.time.LocalDateTime;

public final class PayFixture {

    public static final int DEFAULT_AMOUNT = 100_000;

    private PayFixture() {
    }

    public static Pay createPay(PayMethod method, int amount) {
        return new Pay(method, amount);
    }

    public static PayRequest createPayRequest(PayMethod method, int amount) {
        return new PayRequest(method, amount);
    }

    public static PayCommand createPayCommand(PayMethod method, int amount) {
        return new PayCommand(method, amount);
    }

    public static PayResponse createPayResponse(PayMethod method, int amount, PayStatus status, LocalDateTime paidAt) {
        return new PayResponse(method, amount, status, paidAt);
    }
}
