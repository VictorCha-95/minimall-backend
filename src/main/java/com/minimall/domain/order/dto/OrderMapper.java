package com.minimall.domain.order.dto;

import com.minimall.domain.order.Order;
import com.minimall.domain.order.dto.response.OrderCreateResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface OrderMapper {
    @Mapping(target = "totalAmount", source = "orderAmount.finalAmount")
    OrderSummaryDto toDto(Order order);

    //== Response ==//
    @Mapping(target = "originalAmount", source = "orderAmount.originalAmount")
    @Mapping(target = "discountAmount", source = "orderAmount.discountAmount")
    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    OrderCreateResponseDto toCreateResponse(Order order);
}
