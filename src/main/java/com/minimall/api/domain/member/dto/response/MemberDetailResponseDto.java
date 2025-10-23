package com.minimall.api.domain.member.dto.response;

import com.minimall.api.domain.embeddable.Address;
import com.minimall.api.domain.member.Grade;
import com.minimall.api.domain.member.Member;

public record MemberDetailResponseDto(
        Long id,
        String loginId,
        String name,
        String email,
        Grade grade,
        Address addr
) { }
