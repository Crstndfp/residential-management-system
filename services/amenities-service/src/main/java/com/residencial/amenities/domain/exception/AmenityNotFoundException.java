package com.residencial.amenities.domain.exception;

public class AmenityNotFoundException extends AmenitiesDomainException {
    public AmenityNotFoundException(String message) {
        super("AMENITY_NOT_FOUND", message);
    }
}
