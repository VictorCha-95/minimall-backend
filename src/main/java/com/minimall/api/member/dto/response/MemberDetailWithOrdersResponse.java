package com.minimall.api.member.dto.response;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.CustomerGrade;
import com.minimall.api.order.dto.OrderSummaryDto;

import java.util.List;

public record MemberDetailWithOrdersResponse(
        Long id,
        String loginId,
        String name,
        String email,
        CustomerGrade grade,
        Address addr,
        List<OrderSummaryDto> orders
) {}
