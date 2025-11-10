package com.minimall.domain.order.dto;

import com.minimall.domain.order.Order;
import com.minimall.domain.order.delivery.DeliveryMapper;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import com.minimall.domain.order.dto.response.OrderDetailResponseDto;
import com.minimall.domain.order.dto.response.OrderSummaryResponseDto;
import com.minimall.domain.order.pay.PayMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueMappingStrategy;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring",
        uses = {OrderItemMapper.class, PayMapper.class, DeliveryMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR,
        nullValueIterableMappingStrategy = NullValueMappingStrategy.RETURN_DEFAULT // null 리스트 -> 빈 리스트
)
public interface OrderMapper {
    @Mapping(target = "totalAmount", source = "orderAmount.finalAmount")
    OrderSummaryDto toDto(Order order);

    //== Response ==//
    @Mapping(target = "originalAmount", source = "orderAmount.originalAmount")
    @Mapping(target = "discountAmount", source = "orderAmount.discountAmount")
    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    OrderCreateResponseDto toCreateResponse(Order order);

    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    OrderSummaryResponseDto toOrderSummaryResponse(Order order);

    List<OrderSummaryResponseDto> toOrderSummaryResponse(List<Order> orders);

    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    OrderDetailResponseDto toOrderDetailResponse(Order order);
}
