package com.residencial.amenities.domain.exception;

import com.residencial.amenities.domain.model.MotionStatus;
import java.util.UUID;

public class MotionNotOpenException extends AmenitiesDomainException {
    public MotionNotOpenException(UUID motionId, MotionStatus status) {
        super("MOTION_NOT_OPEN", "Motion " + motionId + " is not open for voting. Current status: " + status);
    }
}
