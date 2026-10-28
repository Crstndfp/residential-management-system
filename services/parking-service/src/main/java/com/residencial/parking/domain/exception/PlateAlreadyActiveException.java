package com.residencial.parking.domain.exception;

public class PlateAlreadyActiveException extends ParkingDomainException {
    public PlateAlreadyActiveException(String plate, String spotNumber) {
        super("PLATE_ALREADY_ACTIVE", "Vehicle with license plate " + plate + " already has an active entry in spot " + spotNumber);
    }
}
