package com.residencial.amenities.domain.exception;

public class DuplicateAmenityCodeException extends AmenitiesDomainException {
    public DuplicateAmenityCodeException(String code) {
        super("DUPLICATE_AMENITY_CODE", "An amenity with code '" + code + "' already exists.");
    }
}
