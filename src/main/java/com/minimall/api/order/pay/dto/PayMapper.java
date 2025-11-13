package com.minimall.api.order.pay.dto;

import com.minimall.domain.order.Pay;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PayMapper {


    PayResponse toPaySummary(Pay pay);

    Pay toEntity(PayRequest requestDto);
}
