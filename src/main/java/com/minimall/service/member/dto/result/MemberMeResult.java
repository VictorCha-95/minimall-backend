package com.minimall.service.member.dto.result;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.CustomerGrade;
import com.minimall.domain.member.MemberStatus;
import com.minimall.domain.member.Role;

public record MemberMeResult(
        Long id,
        String loginId,
        String name,
        String email,
        Role role,
        MemberStatus status,
        CustomerGrade grade,
        Address addr,
        String storeName,
        String businessNumber
) {
}
