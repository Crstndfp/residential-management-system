package com.residencial.finance.domain.exception;

public class FineTypeNotFoundException extends FinanceDomainException {
    public FineTypeNotFoundException(String code) {
        super("FINE_TYPE_NOT_FOUND", "Fine type with code '" + code + "' was not found or is inactive.");
    }
}
