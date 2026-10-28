package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.ApplyParkingFineRequest;
import com.residencial.parking.application.dto.ParkingFineResponse;
import com.residencial.parking.domain.model.ParkingFine;
import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.model.VehicleEntryStatus;
import com.residencial.parking.domain.port.FinanceChargePort;
import com.residencial.parking.domain.port.NotificationDispatcherPort;
import com.residencial.parking.domain.repository.ParkingFineRepository;
import com.residencial.parking.domain.repository.VehicleEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ApplyParkingFineUseCaseTest {

    private ParkingFineRepository fineRepository;
    private VehicleEntryRepository entryRepository;
    private FinanceChargePort financeChargePort;
    private NotificationDispatcherPort notificationDispatcher;
    private ApplyParkingFineUseCase applyParkingFineUseCase;

    @BeforeEach
    void setUp() {
        fineRepository = mock(ParkingFineRepository.class);
        entryRepository = mock(VehicleEntryRepository.class);
        financeChargePort = mock(FinanceChargePort.class);
        notificationDispatcher = mock(NotificationDispatcherPort.class);
        applyParkingFineUseCase = new ApplyParkingFineUseCase(
                fineRepository,
                entryRepository,
                financeChargePort,
                notificationDispatcher
        );
    }

    @Test
    @DisplayName("Successfully applies parking fine, calls finance port and dispatches notification")
    void applyFineSuccess() {
        UUID houseId = UUID.randomUUID();
        UUID adminUserId = UUID.randomUUID();
        UUID mockFinanceChargeId = UUID.randomUUID();

        ApplyParkingFineRequest request = new ApplyParkingFineRequest(
                "P-123XYZ",
                houseId,
                "B-105",
                new BigDecimal("75.00"),
                "Uso indebido de parqueo de visitas",
                adminUserId,
                "Presidente Junta"
        );

        VehicleEntry entry = new VehicleEntry(
                UUID.randomUUID(),
                "P-123XYZ",
                houseId,
                "B-105",
                "Visitor",
                "DOC-123",
                UUID.randomUUID(),
                "P-05",
                UUID.randomUUID(),
                Instant.now().minusSeconds(36000),
                Instant.now(),
                600L,
                VehicleEntryStatus.COMPLETED,
                true,
                Instant.now(),
                Instant.now()
        );

        when(entryRepository.findByPlateOrderByEntryTimeDesc("P-123XYZ")).thenReturn(List.of(entry));
        when(financeChargePort.chargeParkingFine(eq(houseId), eq(new BigDecimal("75.00")), anyString(), eq("P-123XYZ")))
                .thenReturn(mockFinanceChargeId);
        when(fineRepository.save(any(ParkingFine.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ParkingFineResponse response = applyParkingFineUseCase.execute(request);

        assertThat(response).isNotNull();
        assertThat(response.plate()).isEqualTo("P-123XYZ");
        assertThat(response.houseId()).isEqualTo(houseId);
        assertThat(response.fineAmount()).isEqualByComparingTo("75.00");
        assertThat(response.financeChargeId()).isEqualTo(mockFinanceChargeId);
        assertThat(response.status()).isEqualTo("APPLIED");
        assertThat(response.evidenceSummary()).contains("Detectadas 1 pernoctas");

        verify(notificationDispatcher).notifyParkingFineApplied(
                eq(houseId),
                eq("B-105"),
                eq("P-123XYZ"),
                eq(new BigDecimal("75.00")),
                eq("Uso indebido de parqueo de visitas")
        );
    }
}
