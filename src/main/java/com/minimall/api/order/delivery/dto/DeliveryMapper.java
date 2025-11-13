package com.minimall.api.order.delivery.dto;

import com.minimall.api.common.embeddable.AddressMapper;
import com.minimall.domain.order.Delivery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
uses = {AddressMapper.class})
public interface DeliveryMapper {

    DeliverySummaryResponse toDeliverySummary(Delivery delivery);


}
