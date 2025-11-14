package com.minimall.api.order.pay.dto;

import com.minimall.domain.order.Pay;
import com.minimall.service.order.dto.PayCommand;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PayMapper {


    PayResponse toPaySummary(Pay pay);

    Pay toEntity(PayRequest requestDto);

    Pay toEntity(PayCommand payCommand);
}
