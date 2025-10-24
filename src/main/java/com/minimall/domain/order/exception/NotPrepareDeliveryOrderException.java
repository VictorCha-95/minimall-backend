package com.minimall.domain.order.exception;

import com.minimall.domain.order.OrderStatus;

public class NotPrepareDeliveryOrderException extends RuntimeException {
    public NotPrepareDeliveryOrderException(String message) {
        super(message);
    }

  public NotPrepareDeliveryOrderException(Long orderId, OrderStatus currentStatus) {
      super(String.format("배송을 준비할 수 없는 주문 상태 - orderId: %d, Require: CONFIRMED, Current: %s", orderId, currentStatus));
  }
}
