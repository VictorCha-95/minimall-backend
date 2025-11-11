package com.minimall.domain.order.pay.dto;

import com.minimall.domain.order.Pay;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PayMapper {


    PaySummaryDto toPaySummary(Pay pay);

    Pay toEntity(PayRequestDto requestDto);
}
