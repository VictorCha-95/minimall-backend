package com.minimall.api.auth.dto;

import com.minimall.api.common.embeddable.AddressDto;
import com.minimall.domain.member.CustomerGrade;
import com.minimall.domain.member.MemberStatus;
import com.minimall.domain.member.Role;
import com.minimall.service.member.dto.result.MemberMeResult;

public record AuthMeResponse(
        Long id,
        String loginId,
        String name,
        String email,
        Role role,
        MemberStatus status,
        CustomerGrade grade,
        AddressDto addr,
        String storeName,
        String businessNumber
) {
    public static AuthMeResponse from(MemberMeResult result, AddressDto addr) {
        return new AuthMeResponse(
                result.id(),
                result.loginId(),
                result.name(),
                result.email(),
                result.role(),
                result.status(),
                result.grade(),
                addr,
                result.storeName(),
                result.businessNumber()
        );
    }
}
