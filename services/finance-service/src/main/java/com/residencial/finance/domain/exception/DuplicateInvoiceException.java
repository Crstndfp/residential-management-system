package com.residencial.finance.domain.exception;

public class DuplicateInvoiceException extends FinanceDomainException {
    public DuplicateInvoiceException(String invoiceNumber, String taxId) {
        super("DUPLICATE_INVOICE", "Invoice '" + invoiceNumber + "' already exists for vendor with Tax ID '" + taxId + "'.");
    }
}
