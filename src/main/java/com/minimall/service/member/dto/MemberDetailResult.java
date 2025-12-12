package com.minimall.service.member.dto;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Grade;

public record MemberDetailResult(
        Long id,
        String loginId,
        String name,
        String email,
        Grade grade,
        Address addr
) {
}