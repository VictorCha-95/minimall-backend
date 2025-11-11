package com.minimall.domain.order.delivery.dto;

import com.minimall.domain.embeddable.AddressMapper;
import com.minimall.domain.order.Delivery;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring",
uses = {AddressMapper.class})
public interface DeliveryMapper {

    DeliverySummaryDto toDeliverySummary(Delivery delivery);


}
