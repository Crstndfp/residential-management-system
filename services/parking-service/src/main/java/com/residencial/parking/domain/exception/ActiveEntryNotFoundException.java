package com.residencial.parking.domain.exception;

import java.util.UUID;

public class ActiveEntryNotFoundException extends ParkingDomainException {
    public ActiveEntryNotFoundException(UUID entryId) {
        super("ACTIVE_ENTRY_NOT_FOUND", "Active vehicle entry not found for ID: " + entryId);
    }

    public ActiveEntryNotFoundException(String plate) {
        super("ACTIVE_ENTRY_NOT_FOUND", "Active vehicle entry not found for license plate: " + plate);
    }
}
