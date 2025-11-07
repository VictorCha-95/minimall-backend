package com.minimall.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ApiErrorCode {
    VALIDATION_ERROR,        // 바디/파라미터 검증 실패(Bean Validation)
    NOT_FOUND,               // 리소스 없음(회원/상품 등)
    DOMAIN_RULE_VIOLATION,   // 도메인 규칙 위반(재고부족, 상태전이 불가 등)
    DUPLICATE_VALUE,
    CONFLICT,                // 중복/무결성 위반
    INTERNAL_ERROR           // 예상 못한 서버 오류
}