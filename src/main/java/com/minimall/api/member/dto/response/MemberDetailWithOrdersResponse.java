package com.minimall.controller.api.member.dto.response;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Grade;
import com.minimall.controller.api.order.dto.OrderSummaryDto;

import java.util.List;

public record MemberDetailWithOrdersResponse(
        Long id,
        String loginId,
        String name,
        String email,
        Grade grade,
        Address addr,
        List<OrderSummaryDto> orders
) {}
