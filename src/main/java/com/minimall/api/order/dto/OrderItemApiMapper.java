package com.minimall.controller.api.order.dto;

import com.minimall.domain.order.OrderItem;
import com.minimall.controller.api.order.dto.response.OrderItemResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderItemApiMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "totalAmount", expression = "java(item.getTotalAmount())")
    OrderItemResponse toDto(OrderItem item);
}
