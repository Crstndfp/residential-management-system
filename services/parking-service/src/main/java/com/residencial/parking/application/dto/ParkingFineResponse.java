package com.residencial.parking.application.dto;

import com.residencial.parking.domain.model.ParkingFine;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record ParkingFineResponse(
        UUID id,
        String plate,
        UUID houseId,
        String houseNumber,
        BigDecimal fineAmount,
        String reason,
        String evidenceSummary,
        UUID appliedByUserId,
        String appliedByName,
        UUID financeChargeId,
        String status,
        Instant createdAt
) {
    public static ParkingFineResponse fromDomain(ParkingFine fine) {
        return new ParkingFineResponse(
                fine.getId(),
                fine.getPlate(),
                fine.getHouseId(),
                fine.getHouseNumber(),
                fine.getFineAmount(),
                fine.getReason(),
                fine.getEvidenceSummary(),
                fine.getAppliedByUserId(),
                fine.getAppliedByName(),
                fine.getFinanceChargeId(),
                fine.getStatus(),
                fine.getCreatedAt()
        );
    }
}
