package com.minimall.api.exception;

import com.minimall.domain.exception.DomainStatusException;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.service.exception.MemberNotFoundException;
import com.minimall.service.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(MemberNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMemberNotFound(MemberNotFoundException ex) {
        logWarn(ErrorCode.NOT_FOUND_MEMBER, ex);
        return ErrorResponse.toResponse(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND_MEMBER, ex);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        logWarn(ErrorCode.NOT_FOUND, ex);
        return ErrorResponse.toResponse(HttpStatus.NOT_FOUND, ErrorCode.NOT_FOUND, ex);
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateException ex) {
        logWarn(ErrorCode.DUPLICATE_VALUE, ex);
        return ErrorResponse.toResponse(HttpStatus.BAD_REQUEST, ErrorCode.DUPLICATE_VALUE, ex);
    }

    @ExceptionHandler(DomainStatusException.class)
    public ResponseEntity<ErrorResponse> handleDomainStatus(DomainStatusException ex) {
        logWarn(ErrorCode.DOMAIN_STATUS_ERROR, ex);
        return ErrorResponse.toResponse(HttpStatus.BAD_REQUEST, ErrorCode.DOMAIN_STATUS_ERROR, ex);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handle(Exception ex) {
        log.error("[{}] Unexpected exception", ErrorCode.INTERNAL_SERVER_ERROR, ex);
        return ErrorResponse.toResponse(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_SERVER_ERROR, ex);
    }

    //== 공통 로그 메서드 ==//
    private static void logWarn(ErrorCode errorCode, Exception ex) {
        log.warn("[{}] {}", errorCode, ex.getMessage());
    }
}
