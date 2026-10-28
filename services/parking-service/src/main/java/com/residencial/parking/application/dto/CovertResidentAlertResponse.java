package com.residencial.parking.application.dto;

import com.residencial.parking.domain.model.CovertResidentSuspect;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CovertResidentAlertResponse(
        String plate,
        UUID houseId,
        String houseNumber,
        long overnightCount,
        boolean isSuspect,
        String label,
        String suspicionLevel,
        Instant firstDetectedEntry,
        Instant lastDetectedEntry,
        List<VehicleEntryResponse> history
) {
    public static CovertResidentAlertResponse fromDomain(CovertResidentSuspect suspect) {
        List<VehicleEntryResponse> history = suspect.overnightEntries().stream()
                .map(VehicleEntryResponse::fromDomain)
                .toList();

        return new CovertResidentAlertResponse(
                suspect.plate(),
                suspect.houseId(),
                suspect.houseNumber(),
                suspect.overnightCount(),
                suspect.isSuspect(),
                CovertResidentSuspect.SUSPICION_LABEL,
                suspect.suspicionLevel(),
                suspect.firstDetectedEntry(),
                suspect.lastDetectedEntry(),
                history
        );
    }
}
