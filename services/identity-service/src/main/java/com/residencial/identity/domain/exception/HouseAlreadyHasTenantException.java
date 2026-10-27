package com.residencial.identity.domain.exception;

public class HouseAlreadyHasTenantException extends IdentityDomainException {
    public HouseAlreadyHasTenantException(String houseNumber) {
        super("HOUSE_ALREADY_HAS_TENANT", "House '" + houseNumber + "' already has an assigned tenant. Only 1 tenant is permitted per house.");
    }
}
