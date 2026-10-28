package com.residencial.parking.infrastructure.persistence;

import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.model.VehicleEntryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaVehicleEntryRepository extends JpaRepository<VehicleEntry, UUID> {
    Optional<VehicleEntry> findByPlateAndStatus(String plate, VehicleEntryStatus status);

    List<VehicleEntry> findByStatus(VehicleEntryStatus status);

    List<VehicleEntry> findByPlateOrderByEntryTimeDesc(String plate);

    List<VehicleEntry> findByHouseIdOrderByEntryTimeDesc(UUID houseId);

    @Query("SELECT e FROM VehicleEntry e WHERE e.isOvernight = true AND e.entryTime >= :startTime AND e.entryTime <= :endTime ORDER BY e.entryTime ASC")
    List<VehicleEntry> findOvernightEntriesInWindow(@Param("startTime") Instant startTime, @Param("endTime") Instant endTime);
}
