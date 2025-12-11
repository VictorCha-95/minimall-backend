package com.minimall.service.member.dto;

public record MemberCreateCommand(
        String loginId,
        String password,
        String name,
        String email,
        MemberAddressCommand addr
) {
}
