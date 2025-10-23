package com.minimall.api.domain.member.dto.request;

public record MemberUpdatePasswordRequestDto(
        String oldPassword,
        String newPassword
) {}
