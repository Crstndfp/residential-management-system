package com.residencial.parking.domain.exception;

public abstract class ParkingDomainException extends RuntimeException {
    private final String errorCode;

    protected ParkingDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
