package com.residencial.parking.application.dto;

import com.residencial.parking.domain.model.VehicleEntry;

import java.time.Instant;
import java.util.UUID;

public record VehicleExitResponse(
        UUID id,
        String plate,
        String spotNumber,
        Instant entryTime,
        Instant exitTime,
        Long durationMinutes,
        String formattedDuration,
        boolean isOvernight,
        String status
) {
    public static VehicleExitResponse fromDomain(VehicleEntry entry) {
        long minutes = entry.getDurationMinutes() != null ? entry.getDurationMinutes() : 0L;
        long hours = minutes / 60;
        long remMinutes = minutes % 60;
        String formatted = String.format("%d hours, %d minutes", hours, remMinutes);

        return new VehicleExitResponse(
                entry.getId(),
                entry.getPlate(),
                entry.getSpotNumber(),
                entry.getEntryTime(),
                entry.getExitTime(),
                entry.getDurationMinutes(),
                formatted,
                entry.isOvernight(),
                entry.getStatus().name()
        );
    }
}
