package com.residencial.identity.domain.exception;

public class EmailAlreadyInUseException extends IdentityDomainException {
    public EmailAlreadyInUseException(String email) {
        super("EMAIL_ALREADY_IN_USE", "User email '" + email + "' is already in use.");
    }
}
