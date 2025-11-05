package com.minimall.api.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ApiErrorCode {
    NOT_FOUND_MEMBER("NOT_FOUND_MEMBER"),
    NOT_FOUND("NOT_FOUND"),
    DUPLICATE_VALUE("DUPLICATE_VALUE"),
    DOMAIN_STATUS_ERROR("DOMAIN_STATUS_ERROR"),
    INTERNAL_SERVER_ERROR("INTERNAL_SERVER_ERROR");

    private final String code;
}
