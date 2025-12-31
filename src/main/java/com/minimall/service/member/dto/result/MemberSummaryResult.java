package com.minimall.service.member.dto.result;

public record MemberSummaryResult(
        Long id,
        String loginId,
        String name
) {
}
