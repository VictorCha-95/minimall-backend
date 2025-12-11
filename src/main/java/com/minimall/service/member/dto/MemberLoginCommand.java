package com.minimall.service.member.dto;

public record MemberLoginCommand(
        String loginId,
        String password
) {
}
