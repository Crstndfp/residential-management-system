package com.residencial.parking.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VehicleEntryDomainTest {

    @Test
    @DisplayName("ParkingSpot occupy and release lifecycle")
    void parkingSpotOccupancyLifecycle() {
        ParkingSpot spot = new ParkingSpot(
                UUID.randomUUID(),
                "P-05",
                "VISITOR",
                false,
                null,
                Instant.now(),
                Instant.now()
        );

        assertThat(spot.isOccupied()).isFalse();

        UUID entryId = UUID.randomUUID();
        spot.occupy(entryId);

        assertThat(spot.isOccupied()).isTrue();
        assertThat(spot.getCurrentVehicleEntryId()).isEqualTo(entryId);

        // Occupying already occupied spot throws IllegalStateException
        assertThatThrownBy(() -> spot.occupy(UUID.randomUUID()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already occupied");

        spot.release();
        assertThat(spot.isOccupied()).isFalse();
        assertThat(spot.getCurrentVehicleEntryId()).isNull();
    }

    @Test
    @DisplayName("VehicleEntry records exit, computes duration and evaluates overnight status")
    void vehicleEntryExitCalculation() {
        ZoneId zone = ZoneId.of("UTC");
        ZonedDateTime entryDateTime = ZonedDateTime.of(2026, 10, 20, 21, 30, 0, 0, zone);
        ZonedDateTime exitDateTime = ZonedDateTime.of(2026, 10, 21, 6, 45, 0, 0, zone);

        VehicleEntry entry = new VehicleEntry(
                UUID.randomUUID(),
                "P-123XYZ",
                UUID.randomUUID(),
                "A-101",
                "Carlos Guest",
                "DOC-998877",
                UUID.randomUUID(),
                "P-03",
                UUID.randomUUID(),
                entryDateTime.toInstant(),
                null,
                null,
                VehicleEntryStatus.ACTIVE,
                false,
                Instant.now(),
                Instant.now()
        );

        entry.recordExit(exitDateTime.toInstant(), zone);

        assertThat(entry.getStatus()).isEqualTo(VehicleEntryStatus.COMPLETED);
        assertThat(entry.getExitTime()).isEqualTo(exitDateTime.toInstant());
        // Duration: 21:30 to 06:45 = 9 hours 15 mins = 555 minutes
        assertThat(entry.getDurationMinutes()).isEqualTo(555L);
        assertThat(entry.isOvernight()).isTrue();
    }

    @Test
    @DisplayName("Daytime entry is not flagged as overnight")
    void daytimeEntryNotOvernight() {
        ZoneId zone = ZoneId.of("UTC");
        ZonedDateTime entryDateTime = ZonedDateTime.of(2026, 10, 20, 10, 0, 0, 0, zone);
        ZonedDateTime exitDateTime = ZonedDateTime.of(2026, 10, 20, 13, 0, 0, 0, zone);

        VehicleEntry entry = new VehicleEntry(
                UUID.randomUUID(),
                "P-456ABC",
                UUID.randomUUID(),
                "B-202",
                "Day Visitor",
                "DOC-112233",
                UUID.randomUUID(),
                "P-04",
                UUID.randomUUID(),
                entryDateTime.toInstant(),
                null,
                null,
                VehicleEntryStatus.ACTIVE,
                false,
                Instant.now(),
                Instant.now()
        );

        entry.recordExit(exitDateTime.toInstant(), zone);

        assertThat(entry.getDurationMinutes()).isEqualTo(180L);
        assertThat(entry.isOvernight()).isFalse();
    }

    @Test
    @DisplayName("Cannot record exit if exit time is before entry time")
    void exitBeforeEntryThrows() {
        Instant entryTime = Instant.now();
        VehicleEntry entry = new VehicleEntry(
                UUID.randomUUID(),
                "P-789DEF",
                UUID.randomUUID(),
                "C-303",
                "Visitor",
                "DOC-554433",
                UUID.randomUUID(),
                "P-01",
                UUID.randomUUID(),
                entryTime,
                null,
                null,
                VehicleEntryStatus.ACTIVE,
                false,
                Instant.now(),
                Instant.now()
        );

        assertThatThrownBy(() -> entry.recordExit(entryTime.minusSeconds(3600), ZoneId.systemDefault()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Exit time cannot be earlier than entry time");
    }
}
