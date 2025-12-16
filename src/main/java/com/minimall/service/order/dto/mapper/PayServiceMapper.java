package com.minimall.service.order.dto;

import com.minimall.domain.order.Pay;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface PayServiceMapper {

    PayResult toResult(Pay pay);

    Pay toEntity(PayCommand command);
}
