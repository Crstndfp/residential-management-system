package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.CovertResidentAlertResponse;
import com.residencial.parking.domain.model.CovertResidentSuspect;
import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.model.VehicleEntryStatus;
import com.residencial.parking.domain.repository.VehicleEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DetectCovertResidentsUseCaseTest {

    private VehicleEntryRepository entryRepository;
    private DetectCovertResidentsUseCase detectCovertResidentsUseCase;

    @BeforeEach
    void setUp() {
        entryRepository = mock(VehicleEntryRepository.class);
        detectCovertResidentsUseCase = new DetectCovertResidentsUseCase(entryRepository);
    }

    @Test
    @DisplayName("Detects plate with 3 overnight stays as Covert Resident Suspect")
    void detectSuspectPlate() {
        UUID houseId = UUID.randomUUID();
        Instant now = Instant.now();

        VehicleEntry entry1 = createOvernightEntry("P-COVERT01", houseId, "B-205", now.minus(5, ChronoUnit.DAYS));
        VehicleEntry entry2 = createOvernightEntry("P-COVERT01", houseId, "B-205", now.minus(3, ChronoUnit.DAYS));
        VehicleEntry entry3 = createOvernightEntry("P-COVERT01", houseId, "B-205", now.minus(1, ChronoUnit.DAYS));

        // Innocent visitor with only 1 stay
        VehicleEntry entryInnocent = createOvernightEntry("P-NORMAL99", UUID.randomUUID(), "C-101", now.minus(2, ChronoUnit.DAYS));

        when(entryRepository.findOvernightEntriesInWindow(any(Instant.class), any(Instant.class)))
                .thenReturn(List.of(entry1, entry2, entry3, entryInnocent));

        List<CovertResidentAlertResponse> results = detectCovertResidentsUseCase.execute(7);

        assertThat(results).hasSize(2);

        CovertResidentAlertResponse covertSuspect = results.stream()
                .filter(r -> r.plate().equals("P-COVERT01"))
                .findFirst()
                .orElseThrow();

        assertThat(covertSuspect.overnightCount()).isEqualTo(3L);
        assertThat(covertSuspect.isSuspect()).isTrue();
        assertThat(covertSuspect.label()).isEqualTo(CovertResidentSuspect.SUSPICION_LABEL);
        assertThat(covertSuspect.suspicionLevel()).isEqualTo("HIGH");
        assertThat(covertSuspect.history()).hasSize(3);

        CovertResidentAlertResponse normalVisitor = results.stream()
                .filter(r -> r.plate().equals("P-NORMAL99"))
                .findFirst()
                .orElseThrow();

        assertThat(normalVisitor.overnightCount()).isEqualTo(1L);
        assertThat(normalVisitor.isSuspect()).isFalse();
        assertThat(normalVisitor.suspicionLevel()).isEqualTo("NORMAL");
    }

    private VehicleEntry createOvernightEntry(String plate, UUID houseId, String houseNumber, Instant entryTime) {
        return new VehicleEntry(
                UUID.randomUUID(),
                plate,
                houseId,
                houseNumber,
                "Driver",
                "ID-DOC",
                UUID.randomUUID(),
                "P-02",
                UUID.randomUUID(),
                entryTime,
                entryTime.plusSeconds(32400), // 9 hours later
                540L,
                VehicleEntryStatus.COMPLETED,
                true,
                entryTime,
                entryTime.plusSeconds(32400)
        );
    }
}
