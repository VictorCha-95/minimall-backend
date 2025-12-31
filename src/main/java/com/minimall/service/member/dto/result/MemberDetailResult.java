package com.minimall.service.member.dto.result;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.CustomerGrade;

public record MemberDetailResult(
        Long id,
        String loginId,
        String name,
        String email,
        CustomerGrade grade,
        Address addr
) {
}