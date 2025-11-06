package com.minimall.domain.member.dto.request;

import com.minimall.domain.embeddable.Address;
import org.springframework.lang.Nullable;

public record MemberUpdateRequestDto(
        @Nullable String password,
        @Nullable String name,
        @Nullable String email,
        @Nullable Address addr
) {}