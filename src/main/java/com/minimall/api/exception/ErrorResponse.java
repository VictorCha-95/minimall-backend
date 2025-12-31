package com.minimall.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

public record ErrorResponse(
        int status,
        String errorCode,
        String message,
        String path,
        LocalDateTime timestamp,
        List<FieldErrorResponse> errors
) {
    public record FieldErrorResponse(String field, String message) {}

    public static ResponseEntity<ErrorResponse> of(HttpStatus status, ApiErrorCode errorCode, Exception ex, String path) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        status.value(),
                        errorCode.name(),
                        ex.getMessage(),
                        path,
                        LocalDateTime.now(),
                        List.of()
                ));
    }

    public static ResponseEntity<ErrorResponse> of(HttpStatus status, ApiErrorCode errorCode, String message, String path) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        status.value(),
                        errorCode.name(),
                        message,
                        path,
                        LocalDateTime.now(),
                        List.of()
                ));
    }

    public static ResponseEntity<ErrorResponse> of(HttpStatus status, ApiErrorCode errorCode, String message, String path,
                                                   List<FieldErrorResponse> errors) {
        return ResponseEntity
                .status(status)
                .body(new ErrorResponse(
                        status.value(),
                        errorCode.name(),
                        message,
                        path,
                        LocalDateTime.now(),
                        (errors == null ? List.of() : errors)
                ));
    }
}
