package com.residencial.identity.domain.exception;

public class HouseAlreadyHasOwnerException extends IdentityDomainException {
    public HouseAlreadyHasOwnerException(String houseNumber) {
        super("HOUSE_ALREADY_HAS_OWNER", "House '" + houseNumber + "' already has an assigned owner. Only 1 owner is permitted per house.");
    }
}
