package com.residencial.parking.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parking_fines", schema = "parking_schema")
public class ParkingFine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String plate;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Column(name = "fine_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal fineAmount;

    @Column(nullable = false)
    private String reason;

    @Column(name = "evidence_summary", columnDefinition = "TEXT", nullable = false)
    private String evidenceSummary;

    @Column(name = "applied_by_user_id", nullable = false)
    private UUID appliedByUserId;

    @Column(name = "applied_by_name", nullable = false, length = 150)
    private String appliedByName;

    @Column(name = "finance_charge_id")
    private UUID financeChargeId;

    @Column(nullable = false, length = 30)
    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected ParkingFine() {
    }

    public ParkingFine(UUID id, String plate, UUID houseId, String houseNumber,
                       BigDecimal fineAmount, String reason, String evidenceSummary,
                       UUID appliedByUserId, String appliedByName, UUID financeChargeId,
                       String status, Instant createdAt) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be blank");
        }
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null");
        }
        if (fineAmount == null || fineAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fine amount must be greater than zero");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Reason cannot be blank");
        }
        if (evidenceSummary == null || evidenceSummary.isBlank()) {
            throw new IllegalArgumentException("Evidence summary cannot be blank");
        }
        if (appliedByUserId == null) {
            throw new IllegalArgumentException("Applying user ID cannot be null");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.plate = plate.toUpperCase().trim();
        this.houseId = houseId;
        this.houseNumber = houseNumber != null ? houseNumber : "N/A";
        this.fineAmount = fineAmount;
        this.reason = reason;
        this.evidenceSummary = evidenceSummary;
        this.appliedByUserId = appliedByUserId;
        this.appliedByName = appliedByName != null ? appliedByName : "Admin Staff";
        this.financeChargeId = financeChargeId != null ? financeChargeId : UUID.randomUUID();
        this.status = status != null ? status : "APPLIED";
        this.createdAt = createdAt != null ? createdAt : Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getPlate() {
        return plate;
    }

    public UUID getHouseId() {
        return houseId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public BigDecimal getFineAmount() {
        return fineAmount;
    }

    public String getReason() {
        return reason;
    }

    public String getEvidenceSummary() {
        return evidenceSummary;
    }

    public UUID getAppliedByUserId() {
        return appliedByUserId;
    }

    public String getAppliedByName() {
        return appliedByName;
    }

    public UUID getFinanceChargeId() {
        return financeChargeId;
    }

    public String getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
