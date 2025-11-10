package com.minimall.domain.order.dto;

import com.minimall.domain.order.OrderItem;
import com.minimall.domain.order.dto.response.OrderItemResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {

    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "totalAmount", expression = "java(item.getTotalAmount())")
    OrderItemResponseDto toDto(OrderItem item);
}
