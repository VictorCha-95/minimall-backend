package com.minimall.domain.exception;

public abstract class DomainRuleException extends RuntimeException {
    protected DomainRuleException(String message) {
        super(message);
    }
}
