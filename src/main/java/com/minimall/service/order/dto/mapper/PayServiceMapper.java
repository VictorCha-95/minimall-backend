package com.minimall.service.order.dto.mapper;

import com.minimall.domain.order.Pay;
import com.minimall.service.order.dto.command.PayCommand;
import com.minimall.service.order.dto.result.PayResult;
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
