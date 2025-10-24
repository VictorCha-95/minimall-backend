package com.minimall.domain.member.dto.response;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Grade;
import com.minimall.domain.order.dto.OrderSummaryDto;

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
