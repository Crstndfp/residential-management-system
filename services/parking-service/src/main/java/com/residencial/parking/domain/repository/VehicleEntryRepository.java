package com.residencial.parking.domain.repository;

import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.model.VehicleEntryStatus;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VehicleEntryRepository {
    VehicleEntry save(VehicleEntry entry);
    Optional<VehicleEntry> findById(UUID id);
    Optional<VehicleEntry> findActiveByPlate(String plate);
    List<VehicleEntry> findByStatus(VehicleEntryStatus status);
    List<VehicleEntry> findByPlateOrderByEntryTimeDesc(String plate);
    List<VehicleEntry> findByHouseId(UUID houseId);
    List<VehicleEntry> findOvernightEntriesInWindow(Instant startTime, Instant endTime);
}
