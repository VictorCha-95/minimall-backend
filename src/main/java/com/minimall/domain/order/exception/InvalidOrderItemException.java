package com.minimall.domain.order.exception;

import com.minimall.domain.exception.DomainRuleException;
import com.minimall.domain.order.OrderMessage;
import lombok.Getter;

@Getter
public class InvalidOrderItemException extends DomainRuleException {

    public enum Reason {
        REQUIRE_ITEM,       // 주문 항목이 비어있음
        CONTAIN_NULL_ITEM,         // null 항목 포함
        NON_POSITIVE_QUANTITY   // 수량이 1 미만
    }

    private final Reason reason;
    private final Integer index;        // 문제된 항목 인덱스(해당 없으면 null)
    private final Integer requestedQty; // 문제된 수량(해당 없으면 null)

    private InvalidOrderItemException(Reason reason, String message, Integer index, Integer requestedQty) {
        super(message);
        this.reason = reason;
        this.index = index;
        this.requestedQty = requestedQty;
    }

    // == Static factories == //
    public static InvalidOrderItemException require() {
        return new InvalidOrderItemException(
                Reason.REQUIRE_ITEM,
                OrderMessage.REQUIRE_ORDER_ITEM.text(),
                null, null
        );
    }

    public static InvalidOrderItemException nullItemAt(int index) {
        return new InvalidOrderItemException(
                Reason.CONTAIN_NULL_ITEM,
                OrderMessage.NULL_ORDER_ITEM_AT.text(index),
                index, null
        );
    }

    public static InvalidOrderItemException quantityMustBePositive(int requestedQty) {
        return new InvalidOrderItemException(
                Reason.NON_POSITIVE_QUANTITY,
                OrderMessage.INVALID_ORDER_QUANTITY.text(requestedQty),
                null, requestedQty
        );
    }
}
