package com.residencial.parking.domain.exception;

public class NoAvailableParkingSpotException extends ParkingDomainException {
    public NoAvailableParkingSpotException() {
        super("NO_AVAILABLE_PARKING_SPOT", "No visitor parking spots are currently available. All spots are occupied.");
    }
}
