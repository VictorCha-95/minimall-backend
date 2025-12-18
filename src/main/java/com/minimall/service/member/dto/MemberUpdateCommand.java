package com.minimall.service.member.dto;

import com.minimall.domain.embeddable.Address;
import org.springframework.lang.Nullable;

public record MemberUpdateCommand(
        String password,
        String name,
        String email,
        Address addr
) {
}
