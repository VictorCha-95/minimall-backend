package com.minimall.api.member.dto.response;

public record MemberSummaryResponse(
        Long id,
        String loginId,
        String name
) {}
