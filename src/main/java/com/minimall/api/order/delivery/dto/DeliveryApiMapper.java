package com.minimall.api.order.delivery.dto;

import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.service.order.dto.result.DeliverySummaryResult;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        uses = {AddressMapper.class},
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface DeliveryApiMapper {

    DeliverySummaryResponse toDeliverySummaryResponse(DeliverySummaryResult result);


}
