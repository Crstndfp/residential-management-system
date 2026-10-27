package com.residencial.identity.domain.exception;

public class UnauthorizedRoleException extends IdentityDomainException {
    public UnauthorizedRoleException(String message) {
        super("UNAUTHORIZED_ROLE", message);
    }
}
