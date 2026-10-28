package com.residencial.parking.domain.model;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record CovertResidentSuspect(
        String plate,
        UUID houseId,
        String houseNumber,
        long overnightCount,
        boolean isSuspect,
        String suspicionLevel,
        Instant firstDetectedEntry,
        Instant lastDetectedEntry,
        List<VehicleEntry> overnightEntries
) {
    public static final String SUSPICION_LABEL = "SOSPECHA DE RESIDENTE ENCUBIERTO";

    public static CovertResidentSuspect fromEntries(String plate, UUID houseId, String houseNumber, List<VehicleEntry> entries) {
        long count = entries.size();
        boolean suspect = count >= 3;
        String level = count >= 5 ? "CRITICAL" : (count >= 3 ? "HIGH" : "NORMAL");

        Instant first = entries.stream()
                .map(VehicleEntry::getEntryTime)
                .min(Instant::compareTo)
                .orElse(null);

        Instant last = entries.stream()
                .map(VehicleEntry::getEntryTime)
                .max(Instant::compareTo)
                .orElse(null);

        return new CovertResidentSuspect(
                plate,
                houseId,
                houseNumber,
                count,
                suspect,
                level,
                first,
                last,
                entries
        );
    }
}
