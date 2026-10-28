package com.residencial.finance.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fines", schema = "finance_schema")
public class Fine {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fine_type_id", nullable = false)
    private FineType fineType;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @Column(name = "evidence_url", length = 255)
    private String evidenceUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FineStatus status;

    @Column(name = "applied_by_user_id")
    private UUID appliedByUserId;

    @Column(name = "paid_date")
    private LocalDate paidDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Fine() {
        // JPA constructor
    }

    public Fine(UUID houseId, String houseNumber, FineType fineType, BigDecimal customAmount,
                String reason, String evidenceUrl, UUID appliedByUserId) {
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null.");
        }
        if (houseNumber == null || houseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("House number cannot be empty.");
        }
        if (fineType == null) {
            throw new IllegalArgumentException("Fine type cannot be null.");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new IllegalArgumentException("Infraction reason must be provided.");
        }

        BigDecimal finalAmount = customAmount != null ? customAmount : fineType.getDefaultAmount();
        if (finalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Fine amount must be strictly greater than zero.");
        }

        this.id = UUID.randomUUID();
        this.houseId = houseId;
        this.houseNumber = houseNumber.trim();
        this.fineType = fineType;
        this.amount = finalAmount;
        this.reason = reason.trim();
        this.evidenceUrl = evidenceUrl != null ? evidenceUrl.trim() : null;
        this.status = FineStatus.PENDING;
        this.appliedByUserId = appliedByUserId;
        this.paidDate = null;
        this.createdAt = Instant.now();
    }

    public void markAsPaid(LocalDate paidDate) {
        if (this.status == FineStatus.DISMISSED) {
            throw new IllegalStateException("Cannot pay a dismissed fine.");
        }
        if (paidDate == null) {
            throw new IllegalArgumentException("Paid date cannot be null.");
        }
        this.status = FineStatus.PAID;
        this.paidDate = paidDate;
    }

    public void dismiss(String reason) {
        if (this.status == FineStatus.PAID) {
            throw new IllegalStateException("Cannot dismiss an already paid fine.");
        }
        this.status = FineStatus.DISMISSED;
        if (reason != null && !reason.trim().isEmpty()) {
            this.reason = this.reason + "\nDismissal note: " + reason.trim();
        }
    }

    public UUID getId() { return id; }
    public UUID getHouseId() { return houseId; }
    public String getHouseNumber() { return houseNumber; }
    public FineType getFineType() { return fineType; }
    public BigDecimal getAmount() { return amount; }
    public String getReason() { return reason; }
    public String getEvidenceUrl() { return evidenceUrl; }
    public FineStatus getStatus() { return status; }
    public UUID getAppliedByUserId() { return appliedByUserId; }
    public LocalDate getPaidDate() { return paidDate; }
    public Instant getCreatedAt() { return createdAt; }
}
