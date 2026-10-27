package com.residencial.identity.domain.exception;

public class IdentityDomainException extends RuntimeException {

    private final String errorCode;

    public IdentityDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
