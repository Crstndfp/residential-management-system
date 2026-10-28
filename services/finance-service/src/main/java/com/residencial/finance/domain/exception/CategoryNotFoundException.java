package com.residencial.finance.domain.exception;

public class CategoryNotFoundException extends FinanceDomainException {
    public CategoryNotFoundException(String code) {
        super("CATEGORY_NOT_FOUND", "Expense category with code '" + code + "' was not found.");
    }
}
