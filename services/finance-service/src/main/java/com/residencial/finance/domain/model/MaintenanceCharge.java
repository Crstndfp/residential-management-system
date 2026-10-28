package com.residencial.finance.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "maintenance_charges", schema = "finance_schema")
public class MaintenanceCharge {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Column(name = "period_year", nullable = false)
    private int periodYear;

    @Column(name = "period_month", nullable = false)
    private int periodMonth;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "paid_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal paidAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChargeStatus status;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MaintenanceCharge() {
        // JPA constructor
    }

    public MaintenanceCharge(UUID houseId, String houseNumber, int periodYear, int periodMonth,
                             BigDecimal amount, LocalDate dueDate) {
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null.");
        }
        if (houseNumber == null || houseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("House number cannot be empty.");
        }
        if (periodYear < 2000 || periodYear > 2100) {
            throw new IllegalArgumentException("Invalid period year.");
        }
        if (periodMonth < 1 || periodMonth > 12) {
            throw new IllegalArgumentException("Invalid period month (1-12).");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Charge amount must be strictly greater than zero.");
        }
        if (dueDate == null) {
            throw new IllegalArgumentException("Due date cannot be null.");
        }

        this.id = UUID.randomUUID();
        this.houseId = houseId;
        this.houseNumber = houseNumber.trim();
        this.periodYear = periodYear;
        this.periodMonth = periodMonth;
        this.amount = amount;
        this.paidAmount = BigDecimal.ZERO;
        this.status = ChargeStatus.PENDING;
        this.dueDate = dueDate;
        this.createdAt = Instant.now();
    }

    public void applyPayment(BigDecimal payment) {
        if (payment == null || payment.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero.");
        }
        this.paidAmount = this.paidAmount.add(payment);
        if (this.paidAmount.compareTo(this.amount) >= 0) {
            this.status = ChargeStatus.PAID;
        } else {
            this.status = ChargeStatus.PARTIALLY_PAID;
        }
    }

    public UUID getId() { return id; }
    public UUID getHouseId() { return houseId; }
    public String getHouseNumber() { return houseNumber; }
    public int getPeriodYear() { return periodYear; }
    public int getPeriodMonth() { return periodMonth; }
    public BigDecimal getAmount() { return amount; }
    public BigDecimal getPaidAmount() { return paidAmount; }
    public ChargeStatus getStatus() { return status; }
    public LocalDate getDueDate() { return dueDate; }
    public Instant getCreatedAt() { return createdAt; }
}
