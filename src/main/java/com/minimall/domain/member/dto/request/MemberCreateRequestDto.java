package com.minimall.domain.member.dto.request;

import com.minimall.domain.embeddable.Address;

public record MemberCreateRequestDto(
        String loginId,
        String password,
        String name,
        String email,
        Address addr
) {
}
