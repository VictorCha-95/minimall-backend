package com.minimall.fixture;

import com.minimall.api.order.dto.request.OrderCreateRequest;
import com.minimall.api.order.dto.request.OrderItemCreateRequest;
import com.minimall.api.order.delivery.dto.StartDeliveryRequest;
import com.minimall.api.order.dto.request.CompleteDeliveryRequest;
import com.minimall.api.order.dto.response.OrderDetailResponse;
import com.minimall.api.order.dto.response.OrderItemResponse;
import com.minimall.domain.order.OrderStatus;
import com.minimall.service.order.dto.command.OrderCreateCommand;
import com.minimall.service.order.dto.command.OrderItemCreateCommand;
import com.minimall.service.order.dto.result.OrderDetailResult;
import com.minimall.service.order.dto.result.OrderItemResult;

import java.time.LocalDateTime;
import java.util.List;

public final class OrderFixture {

    public static final int DEFAULT_QUANTITY = 10;

    private OrderFixture() {
    }

    public static OrderItemCreateRequest createOrderItemRequest(Long productId, int quantity) {
        return new OrderItemCreateRequest(productId, quantity);
    }

    public static OrderCreateRequest createOrderRequestDto(Long memberId, List<OrderItemCreateRequest> items) {
        return new OrderCreateRequest(memberId, items);
    }

    public static OrderCreateRequest createOrderRequestDto(Long memberId, Long productId, int quantity) {
        return new OrderCreateRequest(memberId, List.of(createOrderItemRequest(productId, quantity)));
    }

    public static OrderItemCreateCommand createOrderItemCommand(Long productId, int quantity) {
        return new OrderItemCreateCommand(productId, quantity);
    }

    public static OrderCreateCommand createOrderCreateCommand(Long memberId, List<OrderItemCreateCommand> items) {
        return new OrderCreateCommand(memberId, items);
    }

    public static OrderCreateCommand createOrderCreateCommand(Long memberId, Long productId, int quantity) {
        return new OrderCreateCommand(memberId, List.of(createOrderItemCommand(productId, quantity)));
    }

    public static OrderItemResult createOrderItemResult(
            Long itemId,
            String productName,
            int price,
            int quantity,
            int totalPrice
    ) {
        return new OrderItemResult(itemId, productName, price, quantity, totalPrice);
    }

    public static OrderDetailResult createOrderDetailResult(
            Long orderId,
            LocalDateTime orderedAt,
            OrderStatus status,
            int totalAmount,
            List<OrderItemResult> items
    ) {
        return new OrderDetailResult(orderId, orderedAt, status, totalAmount, items, null, null);
    }

    public static OrderItemResponse createOrderItemResponse(
            Long itemId,
            String productName,
            int price,
            int quantity,
            int totalPrice
    ) {
        return new OrderItemResponse(itemId, productName, price, quantity, totalPrice);
    }

    public static OrderDetailResponse createOrderDetailResponse(
            Long orderId,
            LocalDateTime orderedAt,
            OrderStatus status,
            int totalAmount,
            List<OrderItemResponse> items
    ) {
        return new OrderDetailResponse(orderId, orderedAt, status, totalAmount, items, null, null);
    }

    public static StartDeliveryRequest createStartDeliveryRequest(String trackingNo, LocalDateTime shippedAt) {
        return new StartDeliveryRequest(trackingNo, shippedAt);
    }

    public static CompleteDeliveryRequest createCompleteDeliveryRequest(LocalDateTime arrivedAt) {
        return new CompleteDeliveryRequest(arrivedAt);
    }
}
