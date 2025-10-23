package com.minimall.api.domain.member.dto.response;

import com.minimall.api.domain.embeddable.Address;
import com.minimall.api.domain.member.Grade;
import com.minimall.api.domain.order.dto.OrderSummaryDto;

import java.util.List;

public record MemberDetailWithOrdersResponseDto(
        Long id,
        String loginId,
        String name,
        String email,
        Grade grade,
        Address addr,
        List<OrderSummaryDto> orders
) {}
