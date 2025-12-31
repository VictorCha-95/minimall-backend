package com.minimall.service.member.dto.command;

import com.minimall.domain.embeddable.Address;

public record MemberUpdateCommand(
        String password,
        String name,
        String email,
        Address addr
) {
}
