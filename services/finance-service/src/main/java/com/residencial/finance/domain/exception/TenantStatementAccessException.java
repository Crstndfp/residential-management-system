package com.residencial.finance.domain.exception;

public class TenantStatementAccessException extends FinanceDomainException {
    public TenantStatementAccessException() {
        super("TENANT_STATEMENT_ACCESS_DENIED", "Access denied: Tenant users are not authorized to view the house account statement.");
    }
}
