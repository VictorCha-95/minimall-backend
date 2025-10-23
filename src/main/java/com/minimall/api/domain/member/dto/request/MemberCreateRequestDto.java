package com.minimall.api.domain.member.dto.request;

import com.minimall.api.domain.embeddable.Address;

public record MemberCreateRequestDto(
        String loginId,
        String password,
        String name,
        String email,
        Address addr
) {
}
