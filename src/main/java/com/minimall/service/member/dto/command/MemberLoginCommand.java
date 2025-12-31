package com.minimall.service.member.dto.command;

public record MemberLoginCommand(
        String loginId,
        String password
) {
}
