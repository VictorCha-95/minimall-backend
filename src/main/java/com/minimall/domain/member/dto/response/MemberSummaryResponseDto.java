package com.minimall.domain.member.dto.response;

public record MemberSummaryResponseDto(
        Long id,
        String loginId,
        String name
) {}
