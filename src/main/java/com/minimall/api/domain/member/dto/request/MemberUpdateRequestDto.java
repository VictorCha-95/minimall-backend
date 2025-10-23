package com.minimall.api.domain.member.dto.request;

import com.minimall.api.domain.embeddable.Address;

public record MemberUpdateRequestDto(
        String name,
        String email,
        Address addr
) {}