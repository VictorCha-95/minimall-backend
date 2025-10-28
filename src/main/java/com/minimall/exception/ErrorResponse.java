package com.minimall.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public record ErrorResponse(
        int status,
        String errorCode,
        String message,
        LocalDateTime timestamp
) {
    public static ResponseEntity<ErrorResponse> toResponse(HttpStatus status, ErrorCode errorCode, Exception ex) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(status.value(), errorCode.getCode(), ex.getMessage(), LocalDateTime.now()));
    }
}
