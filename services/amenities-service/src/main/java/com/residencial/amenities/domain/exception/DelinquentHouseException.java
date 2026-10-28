package com.residencial.amenities.domain.exception;

public class DelinquentHouseException extends AmenitiesDomainException {
    public DelinquentHouseException(String houseNumber) {
        super("HOUSE_DELINQUENT_RESERVATION_BLOCKED",
                "House '" + houseNumber + "' has overdue pending balances (delinquent status). Amenities booking is blocked until account is solvent.");
    }
}
