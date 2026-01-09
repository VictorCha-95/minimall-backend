package com.minimall.service.order.dto.mapper;

import com.minimall.api.order.dto.response.OrderCreateResponse;
import com.minimall.domain.order.Order;
import com.minimall.service.order.dto.result.OrderCreateResult;
import com.minimall.service.order.dto.result.OrderDetailResult;
import com.minimall.service.order.dto.result.OrderSummaryResult;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {OrderItemServiceMapper.class, DeliveryServiceMapper.class, PayServiceMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface OrderServiceMapper {

    @Mapping(target = "originalAmount", source = "orderAmount.originalAmount")
    @Mapping(target = "discountAmount", source = "orderAmount.discountAmount")
    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    @Mapping(target = "itemCount", expression = "java(order.getOrderItems().size())")
    OrderCreateResult toCreateResult(Order order);

    @Mapping(target = "finalAmount", source = "orderAmount.finalAmount")
    OrderDetailResult toDetailResult(Order order);

}