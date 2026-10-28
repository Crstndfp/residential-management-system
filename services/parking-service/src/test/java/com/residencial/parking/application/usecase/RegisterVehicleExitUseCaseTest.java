package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.RegisterVehicleExitRequest;
import com.residencial.parking.application.dto.VehicleExitResponse;
import com.residencial.parking.domain.exception.ActiveEntryNotFoundException;
import com.residencial.parking.domain.model.ParkingSpot;
import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.model.VehicleEntryStatus;
import com.residencial.parking.domain.repository.ParkingSpotRepository;
import com.residencial.parking.domain.repository.VehicleEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RegisterVehicleExitUseCaseTest {

    private VehicleEntryRepository entryRepository;
    private ParkingSpotRepository spotRepository;
    private RegisterVehicleExitUseCase registerVehicleExitUseCase;

    @BeforeEach
    void setUp() {
        entryRepository = mock(VehicleEntryRepository.class);
        spotRepository = mock(ParkingSpotRepository.class);
        registerVehicleExitUseCase = new RegisterVehicleExitUseCase(entryRepository, spotRepository);
    }

    @Test
    @DisplayName("Successfully registers exit by ID, computes duration and releases parking spot")
    void registerExitByIdSuccess() {
        UUID entryId = UUID.randomUUID();
        UUID spotId = UUID.randomUUID();
        Instant entryTime = Instant.now().minusSeconds(7200); // 2 hours ago

        VehicleEntry entry = new VehicleEntry(
                entryId,
                "P-123XYZ",
                UUID.randomUUID(),
                "A-101",
                "Juan Perez",
                "DOC-123",
                spotId,
                "P-04",
                UUID.randomUUID(),
                entryTime,
                null,
                null,
                VehicleEntryStatus.ACTIVE,
                false,
                entryTime,
                entryTime
        );

        ParkingSpot spot = new ParkingSpot(
                spotId,
                "P-04",
                "VISITOR",
                true,
                entryId,
                Instant.now(),
                Instant.now()
        );

        when(entryRepository.findById(entryId)).thenReturn(Optional.of(entry));
        when(spotRepository.findById(spotId)).thenReturn(Optional.of(spot));
        when(entryRepository.save(any(VehicleEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleExitResponse response = registerVehicleExitUseCase.executeById(entryId, new RegisterVehicleExitRequest(null));

        assertThat(response).isNotNull();
        assertThat(response.plate()).isEqualTo("P-123XYZ");
        assertThat(response.spotNumber()).isEqualTo("P-04");
        assertThat(response.status()).isEqualTo("COMPLETED");
        assertThat(response.durationMinutes()).isGreaterThanOrEqualTo(120L);
        assertThat(spot.isOccupied()).isFalse();

        verify(spotRepository).save(spot);
    }

    @Test
    @DisplayName("Throws ActiveEntryNotFoundException when plate has no active entry")
    void exitByPlateNotFoundThrows() {
        when(entryRepository.findActiveByPlate("P-NOTFOUND")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerVehicleExitUseCase.executeByPlate("P-NOTFOUND", null))
                .isInstanceOf(ActiveEntryNotFoundException.class)
                .hasMessageContaining("Active vehicle entry not found");
    }
}
