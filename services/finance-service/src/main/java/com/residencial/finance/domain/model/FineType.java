package com.residencial.finance.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "fine_types", schema = "finance_schema")
public class FineType {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 255)
    private String description;

    @Column(name = "default_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal defaultAmount;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected FineType() {
        // JPA constructor
    }

    public FineType(String code, String name, String description, BigDecimal defaultAmount) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Fine type code cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Fine type name cannot be empty.");
        }
        if (defaultAmount == null || defaultAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Default amount must be strictly greater than zero.");
        }

        this.id = UUID.randomUUID();
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.defaultAmount = defaultAmount;
        this.isActive = true;
        this.createdAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getDefaultAmount() { return defaultAmount; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
}
