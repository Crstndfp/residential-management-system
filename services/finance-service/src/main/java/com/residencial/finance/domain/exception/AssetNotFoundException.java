package com.residencial.finance.domain.exception;

public class AssetNotFoundException extends FinanceDomainException {
    public AssetNotFoundException(String message) {
        super("ASSET_NOT_FOUND", message);
    }
}
