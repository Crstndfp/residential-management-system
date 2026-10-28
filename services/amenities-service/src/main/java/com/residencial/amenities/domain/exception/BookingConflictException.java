package com.residencial.amenities.domain.exception;

public class BookingConflictException extends AmenitiesDomainException {
    public BookingConflictException(String message) {
        super("BOOKING_SLOT_CONFLICT", message);
    }
}
