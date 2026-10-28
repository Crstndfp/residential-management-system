package com.residencial.parking.domain.exception;

public class CovertResidentSuspicionNotMetException extends ParkingDomainException {
    public CovertResidentSuspicionNotMetException(String plate, long overnightCount) {
        super("SUSPICION_THRESHOLD_NOT_MET",
                "Plate " + plate + " has only " + overnightCount + " overnight stay(s). Minimum threshold for covert resident penalty is 3.");
    }
}
