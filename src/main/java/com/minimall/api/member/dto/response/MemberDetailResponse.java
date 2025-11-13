package com.minimall.api.member.dto.response;

import com.minimall.domain.embeddable.Address;
import com.minimall.domain.member.Grade;

public record MemberDetailResponse(
        Long id,
        String loginId,
        String name,
        String email,
        Grade grade,
        Address addr
) { }
