package com.residencial.parking.application.dto;

import com.residencial.parking.domain.model.ParkingSpot;

import java.util.UUID;

public record ParkingSpotStatusResponse(
        UUID id,
        String spotNumber,
        String spotType,
        boolean isOccupied,
        UUID currentVehicleEntryId
) {
    public static ParkingSpotStatusResponse fromDomain(ParkingSpot spot) {
        return new ParkingSpotStatusResponse(
                spot.getId(),
                spot.getSpotNumber(),
                spot.getSpotType(),
                spot.isOccupied(),
                spot.getCurrentVehicleEntryId()
        );
    }
}
