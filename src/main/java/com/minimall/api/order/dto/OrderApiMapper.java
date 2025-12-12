package com.minimall.api.order.dto;

import com.minimall.api.order.pay.dto.PayApiMapper;
import com.minimall.domain.order.Order;
import com.minimall.service.order.dto.DeliveryServiceMapper;
import com.minimall.api.order.dto.response.OrderCreateResponse;
import com.minimall.api.order.dto.response.OrderDetailResponse;
import com.minimall.api.order.dto.response.OrderSummaryResponse;
import com.minimall.service.order.dto.OrderDetailResult;
import com.minimall.service.order.dto.OrderSummaryResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {OrderItemApiMapper.class, PayApiMapper.class, DeliveryServiceMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT // null 리스트 -> 빈 리스트
)
public interface OrderApiMapper {
    @Mapping(target = "totalAmount", source = "orderAmount.finalAmount")
    OrderSummaryDto toDto(Order order);

    //== Response ==//
    @Mapping(target = "originalAmount", source = "orderAmount.originalAmount")
    @Mapping(target = "discountAmount", source = "orderAmount.discountAmount")
    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    OrderCreateResponse toCreateResponse(Order order);

    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    OrderSummaryResponse toOrderSummaryResponse(Order order);
    OrderSummaryResponse toOrderSummaryResponse(OrderSummaryResult result);

    List<OrderSummaryResponse> toOrderSummaryResponse(List<Order> orders);

    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    OrderDetailResponse toOrderDetailResponse(Order order);
    OrderDetailResponse toOrderDetailResponse(OrderDetailResult result);
}
