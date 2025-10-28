package com.minimall.domain.order.dto;

import com.minimall.domain.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {
    @Mapping(target = "totalAmount", source = "orderAmount.finalAmount")
    OrderSummaryDto toDto(Order order);
}
