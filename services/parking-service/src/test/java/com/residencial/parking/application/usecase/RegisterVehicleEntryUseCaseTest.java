package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.RegisterVehicleEntryRequest;
import com.residencial.parking.application.dto.VehicleEntryResponse;
import com.residencial.parking.domain.exception.NoAvailableParkingSpotException;
import com.residencial.parking.domain.exception.PlateAlreadyActiveException;
import com.residencial.parking.domain.model.ParkingSpot;
import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.port.NotificationDispatcherPort;
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

class RegisterVehicleEntryUseCaseTest {

    private VehicleEntryRepository entryRepository;
    private ParkingSpotRepository spotRepository;
    private NotificationDispatcherPort notificationDispatcher;
    private RegisterVehicleEntryUseCase registerVehicleEntryUseCase;

    @BeforeEach
    void setUp() {
        entryRepository = mock(VehicleEntryRepository.class);
        spotRepository = mock(ParkingSpotRepository.class);
        notificationDispatcher = mock(NotificationDispatcherPort.class);
        registerVehicleEntryUseCase = new RegisterVehicleEntryUseCase(
                entryRepository,
                spotRepository,
                notificationDispatcher
        );
    }

    @Test
    @DisplayName("Successfully registers vehicle entry, assigns spot P-01 and dispatches push notification")
    void registerEntrySuccess() {
        UUID houseId = UUID.randomUUID();
        UUID guardUserId = UUID.randomUUID();

        RegisterVehicleEntryRequest request = new RegisterVehicleEntryRequest(
                "P-123XYZ",
                houseId,
                "A-101",
                "Juan Perez",
                "ID-100200",
                guardUserId
        );

        ParkingSpot spot = new ParkingSpot(
                UUID.randomUUID(),
                "P-01",
                "VISITOR",
                false,
                null,
                Instant.now(),
                Instant.now()
        );

        when(entryRepository.findActiveByPlate("P-123XYZ")).thenReturn(Optional.empty());
        when(spotRepository.findFirstAvailableVisitorSpot()).thenReturn(Optional.of(spot));
        when(entryRepository.save(any(VehicleEntry.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(spotRepository.save(any(ParkingSpot.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VehicleEntryResponse response = registerVehicleEntryUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.plate()).isEqualTo("P-123XYZ");
        assertThat(response.spotNumber()).isEqualTo("P-01");
        assertThat(response.status()).isEqualTo("ACTIVE");

        verify(notificationDispatcher).notifyVisitorEntry(
                eq(houseId),
                eq("A-101"),
                eq("P-123XYZ"),
                eq("P-01")
        );
    }

    @Test
    @DisplayName("Throws PlateAlreadyActiveException when vehicle is already parked inside")
    void plateAlreadyActiveThrows() {
        RegisterVehicleEntryRequest request = new RegisterVehicleEntryRequest(
                "P-123XYZ",
                UUID.randomUUID(),
                "A-101",
                "Juan Perez",
                "ID-100200",
                UUID.randomUUID()
        );

        VehicleEntry existingEntry = new VehicleEntry(
                UUID.randomUUID(),
                "P-123XYZ",
                UUID.randomUUID(),
                "A-101",
                "Juan Perez",
                "ID-100200",
                UUID.randomUUID(),
                "P-05",
                UUID.randomUUID(),
                Instant.now(),
                null,
                null,
                null,
                false,
                Instant.now(),
                Instant.now()
        );

        when(entryRepository.findActiveByPlate("P-123XYZ")).thenReturn(Optional.of(existingEntry));

        assertThatThrownBy(() -> registerVehicleEntryUseCase.execute(request))
                .isInstanceOf(PlateAlreadyActiveException.class)
                .hasMessageContaining("already has an active entry");
    }

    @Test
    @DisplayName("Throws NoAvailableParkingSpotException when all visitor spots are full")
    void allSpotsOccupiedThrows() {
        RegisterVehicleEntryRequest request = new RegisterVehicleEntryRequest(
                "P-999ZZZ",
                UUID.randomUUID(),
                "B-202",
                "Visitor",
                "ID-333444",
                UUID.randomUUID()
        );

        when(entryRepository.findActiveByPlate("P-999ZZZ")).thenReturn(Optional.empty());
        when(spotRepository.findFirstAvailableVisitorSpot()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registerVehicleEntryUseCase.execute(request))
                .isInstanceOf(NoAvailableParkingSpotException.class)
                .hasMessageContaining("No visitor parking spots are currently available");
    }
}
