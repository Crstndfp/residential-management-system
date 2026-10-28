package com.residencial.parking.application.usecase;

import com.residencial.parking.application.dto.ApplyParkingFineRequest;
import com.residencial.parking.application.dto.ParkingFineResponse;
import com.residencial.parking.domain.model.ParkingFine;
import com.residencial.parking.domain.model.VehicleEntry;
import com.residencial.parking.domain.port.FinanceChargePort;
import com.residencial.parking.domain.port.NotificationDispatcherPort;
import com.residencial.parking.domain.repository.ParkingFineRepository;
import com.residencial.parking.domain.repository.VehicleEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class ApplyParkingFineUseCase {

    private final ParkingFineRepository fineRepository;
    private final VehicleEntryRepository entryRepository;
    private final FinanceChargePort financeChargePort;
    private final NotificationDispatcherPort notificationDispatcher;

    public ApplyParkingFineUseCase(ParkingFineRepository fineRepository,
                                  VehicleEntryRepository entryRepository,
                                  FinanceChargePort financeChargePort,
                                  NotificationDispatcherPort notificationDispatcher) {
        this.fineRepository = fineRepository;
        this.entryRepository = entryRepository;
        this.financeChargePort = financeChargePort;
        this.notificationDispatcher = notificationDispatcher;
    }

    @Transactional
    public ParkingFineResponse execute(ApplyParkingFineRequest request) {
        String normalizedPlate = request.plate().toUpperCase().trim();

        BigDecimal amount = (request.fineAmount() != null && request.fineAmount().compareTo(BigDecimal.ZERO) > 0)
                ? request.fineAmount()
                : new BigDecimal("50.00");

        String reason = (request.reason() != null && !request.reason().isBlank())
                ? request.reason()
                : "Uso indebido de parqueo de visitas - Pernocta no autorizada (Residente Encubierto)";

        // Compile evidence summary from overnight records
        List<VehicleEntry> entries = entryRepository.findByPlateOrderByEntryTimeDesc(normalizedPlate);
        long overnightCount = entries.stream().filter(VehicleEntry::isOvernight).count();

        String evidenceSummary = String.format(
                "Detectadas %d pernoctas para la placa %s asociadas a la vivienda %s. Registros: [%s]",
                overnightCount,
                normalizedPlate,
                request.houseNumber(),
                entries.stream()
                        .filter(VehicleEntry::isOvernight)
                        .limit(5)
                        .map(e -> String.format("Entrada: %s, Salida: %s", e.getEntryTime(), e.getExitTime()))
                        .collect(Collectors.joining("; "))
        );

        // Charge fine to house account in finance-service
        UUID financeChargeId = financeChargePort.chargeParkingFine(
                request.houseId(),
                amount,
                reason,
                normalizedPlate
        );

        ParkingFine fine = new ParkingFine(
                UUID.randomUUID(),
                normalizedPlate,
                request.houseId(),
                request.houseNumber(),
                amount,
                reason,
                evidenceSummary,
                request.appliedByUserId(),
                request.appliedByName(),
                financeChargeId,
                "APPLIED",
                Instant.now()
        );

        ParkingFine saved = fineRepository.save(fine);

        // Dispatch push notification to house owner
        notificationDispatcher.notifyParkingFineApplied(
                request.houseId(),
                request.houseNumber(),
                normalizedPlate,
                amount,
                reason
        );

        return ParkingFineResponse.fromDomain(saved);
    }
}
