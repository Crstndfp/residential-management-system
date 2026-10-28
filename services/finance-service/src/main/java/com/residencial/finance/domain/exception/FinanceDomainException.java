package com.residencial.finance.domain.exception;

public class FinanceDomainException extends RuntimeException {

    private final String errorCode;

    public FinanceDomainException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
