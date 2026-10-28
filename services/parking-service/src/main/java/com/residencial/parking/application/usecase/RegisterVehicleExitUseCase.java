package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.RegisterVehicleExitRequest;
import com.residencial.parking.application.dto.VehicleExitResponse;
import com.residencial.parking.domain.exception.ActiveEntryNotFoundException;
import com.residencial.parking.domain.model.ParkingSpot;
import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.model.VehicleEntryStatus;
import com.residencial.parking.domain.repository.ParkingSpotRepository;
import com.residencial.parking.domain.repository.VehicleEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;

@Service
public class RegisterVehicleExitUseCase {

    private final VehicleEntryRepository entryRepository;
    private final ParkingSpotRepository spotRepository;

    public RegisterVehicleExitUseCase(VehicleEntryRepository entryRepository,
                                     ParkingSpotRepository spotRepository) {
        this.entryRepository = entryRepository;
        this.spotRepository = spotRepository;
    }

    @Transactional
    public VehicleExitResponse executeById(UUID entryId, RegisterVehicleExitRequest request) {
        VehicleEntry entry = entryRepository.findById(entryId)
                .filter(e -> e.getStatus() == VehicleEntryStatus.ACTIVE)
                .orElseThrow(() -> new ActiveEntryNotFoundException(entryId));

        return processExit(entry, request);
    }

    @Transactional
    public VehicleExitResponse executeByPlate(String plate, RegisterVehicleExitRequest request) {
        String normalizedPlate = plate.toUpperCase().trim();
        VehicleEntry entry = entryRepository.findActiveByPlate(normalizedPlate)
                .orElseThrow(() -> new ActiveEntryNotFoundException(normalizedPlate));

        return processExit(entry, request);
    }

    private VehicleExitResponse processExit(VehicleEntry entry, RegisterVehicleExitRequest request) {
        Instant exitTime = (request != null && request.exitTime() != null) ? request.exitTime() : Instant.now();

        entry.recordExit(exitTime, ZoneId.systemDefault());

        if (entry.getParkingSpotId() != null) {
            spotRepository.findById(entry.getParkingSpotId()).ifPresent(spot -> {
                spot.release();
                spotRepository.save(spot);
            });
        } else if (entry.getSpotNumber() != null) {
            spotRepository.findBySpotNumber(entry.getSpotNumber()).ifPresent(spot -> {
                spot.release();
                spotRepository.save(spot);
            });
        }

        VehicleEntry saved = entryRepository.save(entry);
        return VehicleExitResponse.fromDomain(saved);
    }
}
