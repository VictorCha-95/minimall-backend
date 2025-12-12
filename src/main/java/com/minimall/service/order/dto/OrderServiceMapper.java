package com.minimall.service.order.dto;

import com.minimall.domain.order.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {OrderItemServiceMapper.class, DeliveryServiceMapper.class, PayMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderServiceMapper {

    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    OrderDetailResult toDetailResult(Order order);

    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    OrderSummaryResult toSummaryResult(Order order);

    List<OrderSummaryResult> toSummaryResultList(List<Order> orders);
}