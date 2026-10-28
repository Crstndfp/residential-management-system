package com.residencial.parking.application.dto;

import com.residencial.parking.domain.model.VehicleEntry;

import java.time.Instant;
import java.util.UUID;

public record VehicleEntryResponse(
        UUID id,
        String plate,
        UUID houseId,
        String houseNumber,
        String visitorName,
        String visitorIdDocument,
        UUID parkingSpotId,
        String spotNumber,
        UUID guardUserId,
        Instant entryTime,
        Instant exitTime,
        Long durationMinutes,
        String status,
        boolean isOvernight,
        Instant createdAt
) {
    public static VehicleEntryResponse fromDomain(VehicleEntry entry) {
        return new VehicleEntryResponse(
                entry.getId(),
                entry.getPlate(),
                entry.getHouseId(),
                entry.getHouseNumber(),
                entry.getVisitorName(),
                entry.getVisitorIdDocument(),
                entry.getParkingSpotId(),
                entry.getSpotNumber(),
                entry.getGuardUserId(),
                entry.getEntryTime(),
                entry.getExitTime(),
                entry.getDurationMinutes(),
                entry.getStatus().name(),
                entry.isOvernight(),
                entry.getCreatedAt()
        );
    }
}
