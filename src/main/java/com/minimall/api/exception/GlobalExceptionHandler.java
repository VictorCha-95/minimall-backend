package com.minimall.api.exception;

import com.minimall.domain.exception.DomainRuleException;
import com.minimall.domain.exception.DuplicateException;
import com.minimall.service.exception.InvalidCredentialException;
import com.minimall.service.exception.NotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 400: @RequestBody 검증 실패(@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidBody(
            MethodArgumentNotValidException ex, HttpServletRequest req) {

        List<ErrorResponse.FieldErrorResponse> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(this::toFieldError)
                .toList();

        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                "Validation failed",
                req.getRequestURI(),
                errors
        );
    }

    private ErrorResponse.FieldErrorResponse toFieldError(FieldError fe) {
        return new ErrorResponse.FieldErrorResponse(
                fe.getField(),
                fe.getDefaultMessage()
        );
    }

    // 400: @RequestParam / @PathVariable 검증 실패(@Validated)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
            ConstraintViolationException ex, HttpServletRequest req) {

        List<ErrorResponse.FieldErrorResponse> errors = ex.getConstraintViolations()
                .stream()
                .map(this::toFieldError)
                .toList();

        return ErrorResponse.of(
                HttpStatus.BAD_REQUEST,
                ApiErrorCode.VALIDATION_ERROR,
                "Validation failed",
                req.getRequestURI(),
                errors
        );
    }

    private ErrorResponse.FieldErrorResponse toFieldError(ConstraintViolation<?> v) {
        String path = (v.getPropertyPath() == null) ? "" : v.getPropertyPath().toString();
        String field = path.contains(".") ? path.substring(path.lastIndexOf('.') + 1) : path;
        return new ErrorResponse.FieldErrorResponse(field, v.getMessage());
    }

    @ExceptionHandler(InvalidCredentialException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredential(
            InvalidCredentialException ex, HttpServletRequest req) {
        return ErrorResponse.of(HttpStatus.UNAUTHORIZED, ApiErrorCode.INVALID_CREDENTIALS, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
            NotFoundException ex, HttpServletRequest req) {
        return ErrorResponse.of(HttpStatus.NOT_FOUND, ApiErrorCode.NOT_FOUND, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
            DataIntegrityViolationException ex, HttpServletRequest req) {
        return ErrorResponse.of(HttpStatus.CONFLICT, ApiErrorCode.CONFLICT, "Duplicate or constraint violation", req.getRequestURI());
    }

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(
            DuplicateException ex, HttpServletRequest req) {
        return ErrorResponse.of(HttpStatus.CONFLICT, ApiErrorCode.DUPLICATE_VALUE, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(DomainRuleException.class)
    public ResponseEntity<ErrorResponse> handleDomainRule(
            DomainRuleException ex, HttpServletRequest req) {
        return ErrorResponse.of(HttpStatus.UNPROCESSABLE_ENTITY, ApiErrorCode.DOMAIN_RULE_VIOLATION, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotAllowed(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        return ErrorResponse.of(HttpStatus.METHOD_NOT_ALLOWED, ApiErrorCode.METHOD_NOT_ALLOWED, ex.getMessage(), req.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleEtc(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception: {} {}", req.getMethod(), req.getRequestURI(), ex);
        return ErrorResponse.of(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.INTERNAL_ERROR, "Unexpected server error", req.getRequestURI());
    }
}
