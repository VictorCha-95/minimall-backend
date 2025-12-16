package com.minimall.controller.api.member.dto.request;

import com.minimall.domain.embeddable.Address;
import org.springframework.lang.Nullable;

public record MemberUpdateRequest(
        @Nullable String password,
        @Nullable String name,
        @Nullable String email,
        @Nullable Address addr
) {}