package com.residencial.parking.infrastructure.persistence;

import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.model.VehicleEntryStatus;
import com.residencial.parking.domain.repository.VehicleEntryRepository;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class VehicleEntryRepositoryAdapter implements VehicleEntryRepository {

    private final JpaVehicleEntryRepository jpaEntryRepository;

    public VehicleEntryRepositoryAdapter(JpaVehicleEntryRepository jpaEntryRepository) {
        this.jpaEntryRepository = jpaEntryRepository;
    }

    @Override
    public VehicleEntry save(VehicleEntry entry) {
        return jpaEntryRepository.save(entry);
    }

    @Override
    public Optional<VehicleEntry> findById(UUID id) {
        return jpaEntryRepository.findById(id);
    }

    @Override
    public Optional<VehicleEntry> findActiveByPlate(String plate) {
        return jpaEntryRepository.findByPlateAndStatus(plate.toUpperCase().trim(), VehicleEntryStatus.ACTIVE);
    }

    @Override
    public List<VehicleEntry> findByStatus(VehicleEntryStatus status) {
        return jpaEntryRepository.findByStatus(status);
    }

    @Override
    public List<VehicleEntry> findByPlateOrderByEntryTimeDesc(String plate) {
        return jpaEntryRepository.findByPlateOrderByEntryTimeDesc(plate.toUpperCase().trim());
    }

    @Override
    public List<VehicleEntry> findByHouseId(UUID houseId) {
        return jpaEntryRepository.findByHouseIdOrderByEntryTimeDesc(houseId);
    }

    @Override
    public List<VehicleEntry> findOvernightEntriesInWindow(Instant startTime, Instant endTime) {
        return jpaEntryRepository.findOvernightEntriesInWindow(startTime, endTime);
    }
}
