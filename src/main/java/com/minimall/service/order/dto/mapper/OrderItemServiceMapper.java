package com.minimall.service.order.dto;

import com.minimall.domain.order.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderItemServiceMapper {

    @Mapping(target = "productId", source = "product.id")
    OrderItemResult toResult(OrderItem orderItem);
}
