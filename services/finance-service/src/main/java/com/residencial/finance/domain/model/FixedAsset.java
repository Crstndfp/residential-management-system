package com.residencial.finance.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "fixed_assets", schema = "finance_schema")
public class FixedAsset {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private AssetCategory category;

    @Column(name = "physical_location", nullable = false, length = 150)
    private String physicalLocation;

    @Column(name = "acquisition_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal acquisitionCost;

    @Column(name = "acquisition_date", nullable = false)
    private LocalDate acquisitionDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AssetStatus status;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected FixedAsset() {
        // JPA constructor
    }

    public FixedAsset(String code, String name, AssetCategory category,
                      String physicalLocation, BigDecimal acquisitionCost,
                      LocalDate acquisitionDate, AssetStatus status, String notes) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset code cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Asset name cannot be empty.");
        }
        if (category == null) {
            throw new IllegalArgumentException("Asset category cannot be null.");
        }
        if (physicalLocation == null || physicalLocation.trim().isEmpty()) {
            throw new IllegalArgumentException("Physical location cannot be empty.");
        }
        if (acquisitionCost == null || acquisitionCost.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Acquisition cost must be strictly greater than zero.");
        }
        if (acquisitionDate == null) {
            throw new IllegalArgumentException("Acquisition date cannot be null.");
        }

        this.id = UUID.randomUUID();
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.category = category;
        this.physicalLocation = physicalLocation.trim();
        this.acquisitionCost = acquisitionCost;
        this.acquisitionDate = acquisitionDate;
        this.status = status != null ? status : AssetStatus.EXCELLENT;
        this.notes = notes != null ? notes.trim() : null;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateStatus(AssetStatus newStatus, String notes) {
        if (newStatus == null) {
            throw new IllegalArgumentException("New status cannot be null.");
        }
        this.status = newStatus;
        if (notes != null && !notes.trim().isEmpty()) {
            this.notes = notes.trim();
        }
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public AssetCategory getCategory() { return category; }
    public String getPhysicalLocation() { return physicalLocation; }
    public BigDecimal getAcquisitionCost() { return acquisitionCost; }
    public LocalDate getAcquisitionDate() { return acquisitionDate; }
    public AssetStatus getStatus() { return status; }
    public String getNotes() { return notes; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
