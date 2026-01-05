package com.minimall.service.exception;

public class InvalidCredentialException extends RuntimeException {
    private InvalidCredentialException(String message) {
        super(message);
    }

    public InvalidCredentialException() {
    }

    public static InvalidCredentialException invalidLoginId(String loginId){
        return new InvalidCredentialException("Invalid LoginId: " + loginId);
    }

    public static InvalidCredentialException invalidPassword(String password){
        return new InvalidCredentialException("Invalid Password: " + password);
    }
}
