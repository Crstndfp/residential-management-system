package com.residencial.identity.domain.exception;

public class HouseAlreadyExistsException extends IdentityDomainException {
    public HouseAlreadyExistsException(String houseNumber) {
        super("HOUSE_ALREADY_EXISTS", "House with number '" + houseNumber + "' already exists.");
    }
}
