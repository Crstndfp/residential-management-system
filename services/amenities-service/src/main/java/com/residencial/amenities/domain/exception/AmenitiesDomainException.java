package com.residencial.amenities.domain.exception;

public abstract class AmenitiesDomainException extends RuntimeException {
    private final String errorCode;

    protected AmenitiesDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
