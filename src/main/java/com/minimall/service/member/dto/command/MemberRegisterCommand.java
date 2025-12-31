package com.minimall.service.member.dto.command;

public record MemberRegisterCommand(
        String loginId,
        String password,
        String name,
        String email,
        MemberAddressCommand addr
)
{ }
