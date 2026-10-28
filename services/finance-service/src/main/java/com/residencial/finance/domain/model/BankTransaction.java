package com.residencial.finance.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "bank_transactions", schema = "finance_schema")
public class BankTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "authorization_number", nullable = false, unique = true, length = 50)
    private String authorizationNumber;

    @Column(name = "house_reference", nullable = false, length = 50)
    private String houseReference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matched_charge_id")
    private MaintenanceCharge matchedCharge;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "source_file_name", length = 150)
    private String sourceFileName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BankTransactionStatus status;

    @Column(name = "discrepancy_reason", length = 255)
    private String discrepancyReason;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected BankTransaction() {
        // JPA constructor
    }

    public BankTransaction(String authorizationNumber, String houseReference,
                           MaintenanceCharge matchedCharge, LocalDate transactionDate,
                           BigDecimal amount, String sourceFileName,
                           BankTransactionStatus status, String discrepancyReason) {
        if (authorizationNumber == null || authorizationNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("Authorization number cannot be empty.");
        }
        if (houseReference == null || houseReference.trim().isEmpty()) {
            throw new IllegalArgumentException("House reference cannot be empty.");
        }
        if (transactionDate == null) {
            throw new IllegalArgumentException("Transaction date cannot be null.");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be strictly greater than zero.");
        }
        if (status == null) {
            throw new IllegalArgumentException("Transaction status cannot be null.");
        }

        this.id = UUID.randomUUID();
        this.authorizationNumber = authorizationNumber.trim();
        this.houseReference = houseReference.trim();
        this.matchedCharge = matchedCharge;
        this.transactionDate = transactionDate;
        this.amount = amount;
        this.sourceFileName = sourceFileName != null ? sourceFileName.trim() : null;
        this.status = status;
        this.discrepancyReason = discrepancyReason != null ? discrepancyReason.trim() : null;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getAuthorizationNumber() { return authorizationNumber; }
    public String getHouseReference() { return houseReference; }
    public MaintenanceCharge getMatchedCharge() { return matchedCharge; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public BigDecimal getAmount() { return amount; }
    public String getSourceFileName() { return sourceFileName; }
    public BankTransactionStatus getStatus() { return status; }
    public String getDiscrepancyReason() { return discrepancyReason; }
    public Instant getCreatedAt() { return createdAt; }
}
