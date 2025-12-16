package com.minimall.controller.api.member.dto.response;

public record MemberSummaryResponse(
        Long id,
        String loginId,
        String name
) {}
