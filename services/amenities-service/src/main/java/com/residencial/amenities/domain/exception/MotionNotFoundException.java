package com.residencial.amenities.domain.exception;

import java.util.UUID;

public class MotionNotFoundException extends AmenitiesDomainException {
    public MotionNotFoundException(UUID id) {
        super("MOTION_NOT_FOUND", "Motion not found with ID: " + id);
    }
}
