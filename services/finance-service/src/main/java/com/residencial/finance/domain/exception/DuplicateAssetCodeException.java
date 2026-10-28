package com.residencial.finance.domain.exception;

public class DuplicateAssetCodeException extends FinanceDomainException {
    public DuplicateAssetCodeException(String code) {
        super("DUPLICATE_ASSET_CODE", "An asset with code '" + code + "' already exists in inventory.");
    }
}
