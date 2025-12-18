package com.minimall.service.order.dto.mapper;

import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.domain.order.Delivery;
import com.minimall.service.order.dto.result.DeliverySummaryResult;
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
