package com.minimall.api.exception;

import com.minimall.domain.exception.DomainRuleException;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.service.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 400: @RequestBody DTO 검증 실패(@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                "Validation failed",
                req.getRequestURI()
        );
    }

    // 404: 리소스 없음 (서비스/리포지토리에서 던지는 NotFound 계열)
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex, HttpServletRequest req) {
        return ErrorResponse.of(
                HttpStatus.NOT_FOUND,
                ApiErrorCode.NOT_FOUND,
                ex.getMessage(),
                req.getRequestURI()
        );
    }

    // 409: DB 제약 위반(UNIQUE 등 무결성 위반)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            DataIntegrityViolationException ex, HttpServletRequest req) {
        return ErrorResponse.of(
                HttpStatus.CONFLICT,
                ApiErrorCode.CONFLICT,
                "Duplicate or constraint violation",
                req.getRequestURI()
        );
    }

    // 409: App 중복 위반
    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateException ex, HttpServletRequest req) {
        return ErrorResponse.of(
                HttpStatus.CONFLICT,
                ApiErrorCode.DUPLICATE_VALUE,
                ex.getMessage(),
                req.getRequestURI()
        );
    }

    // 422: 도메인 규칙 위반
    @ExceptionHandler(DomainRuleException.class)
    public ResponseEntity<ErrorResponse> handleDomainRule(
            DomainRuleException ex, HttpServletRequest req) {
        return ErrorResponse.of(
                HttpStatus.UNPROCESSABLE_ENTITY,
                ApiErrorCode.DOMAIN_RULE_VIOLATION,
                ex.getMessage(),
                req.getRequestURI()
        );
    }

    // 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception ex, HttpServletRequest req) {
        return ErrorResponse.of(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiErrorCode.INTERNAL_ERROR,
                "Unexpected server error",
                req.getRequestURI()
        );
    }
}
