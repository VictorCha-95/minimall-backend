package com.minimall.service.member.dto;

public record MemberSummaryResult(
        Long id,
        String loginId,
        String name
) {
}
