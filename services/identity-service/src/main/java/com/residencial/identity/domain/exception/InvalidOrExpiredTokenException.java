package com.residencial.identity.domain.exception;

public class InvalidOrExpiredTokenException extends IdentityDomainException {
    public InvalidOrExpiredTokenException() {
        super("INVALID_OR_EXPIRED_TOKEN", "The provided invitation token is invalid, has expired, or was already used.");
    }
}
