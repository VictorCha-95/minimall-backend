package com.minimall.service.order.dto;

import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.domain.order.Delivery;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {AddressMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface DeliveryServiceMapper {

    DeliverySummaryResult toDeliverySummary(Delivery delivery);


}
