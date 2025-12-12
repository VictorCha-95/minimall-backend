package com.minimall.api.order.pay.dto;

import com.minimall.domain.order.Pay;
import com.minimall.service.order.dto.PayCommand;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PayApiMapper {


    PayResponse toPaySummary(Pay pay);

    Pay toEntity(PayRequest requestDto);

    Pay toEntity(PayCommand payCommand);
}
