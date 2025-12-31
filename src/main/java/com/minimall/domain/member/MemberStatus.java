package com.minimall.domain.member;

public enum MemberStatus {
    ACTIVE,
    SUSPENDED,      // 정지
    DELETED,        // 탈퇴
    PENDING         // 이메일 인증 전 등
}
