package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.RegisterVehicleEntryRequest;
import com.residencial.parking.application.dto.VehicleEntryResponse;
import com.residencial.parking.domain.exception.NoAvailableParkingSpotException;
import com.residencial.parking.domain.exception.PlateAlreadyActiveException;
import com.residencial.parking.domain.model.ParkingSpot;
import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.model.VehicleEntryStatus;
import com.residencial.parking.domain.port.NotificationDispatcherPort;
import com.residencial.parking.domain.repository.ParkingSpotRepository;
import com.residencial.parking.domain.repository.VehicleEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class RegisterVehicleEntryUseCase {

    private final VehicleEntryRepository entryRepository;
    private final ParkingSpotRepository spotRepository;
    private final NotificationDispatcherPort notificationDispatcher;

    public RegisterVehicleEntryUseCase(VehicleEntryRepository entryRepository,
                                      ParkingSpotRepository spotRepository,
                                      NotificationDispatcherPort notificationDispatcher) {
        this.entryRepository = entryRepository;
        this.spotRepository = spotRepository;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Transactional
    public VehicleEntryResponse execute(RegisterVehicleEntryRequest request) {
        String normalizedPlate = request.plate().toUpperCase().trim();

        Optional<VehicleEntry> activeEntry = entryRepository.findActiveByPlate(normalizedPlate);
        if (activeEntry.isPresent()) {
            throw new PlateAlreadyActiveException(normalizedPlate, activeEntry.get().getSpotNumber());
        }

        ParkingSpot availableSpot = spotRepository.findFirstAvailableVisitorSpot()
                .orElseThrow(NoAvailableParkingSpotException::new);

        UUID entryId = UUID.randomUUID();
        VehicleEntry entry = new VehicleEntry(
                entryId,
                normalizedPlate,
                request.houseId(),
                request.houseNumber(),
                request.visitorName(),
                request.visitorIdDocument(),
                availableSpot.getId(),
                availableSpot.getSpotNumber(),
                request.guardUserId(),
                Instant.now(),
                null,
                null,
                VehicleEntryStatus.ACTIVE,
                false,
                Instant.now(),
                Instant.now()
        );

        availableSpot.occupy(entryId);

        VehicleEntry saved = entryRepository.save(entry);
        spotRepository.save(availableSpot);

        // Dispatch push notification to visited house
        notificationDispatcher.notifyVisitorEntry(
                request.houseId(),
                request.houseNumber(),
                normalizedPlate,
                availableSpot.getSpotNumber()
        );

        return VehicleEntryResponse.fromDomain(saved);
    }
}
