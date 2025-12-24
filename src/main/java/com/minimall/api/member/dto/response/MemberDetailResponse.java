package com.minimall.api.member.dto.response;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.CustomerGrade;

public record MemberDetailResponse(
        Long id,
        String loginId,
        String name,
        String email,
        CustomerGrade grade,
        Address addr
) { }
