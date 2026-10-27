package com.residencial.identity.domain.exception;

import java.util.UUID;

public class HouseNotFoundException extends IdentityDomainException {
    public HouseNotFoundException(UUID houseId) {
        super("HOUSE_NOT_FOUND", "House with ID '" + houseId + "' was not found.");
    }
}
