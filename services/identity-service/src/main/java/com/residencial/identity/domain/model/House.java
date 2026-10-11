package com.residencial.identity.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "houses", schema = "identity_schema")
public class House {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "house_number", nullable = false, length = 20, unique = true)
    private String houseNumber;

    @Column(name = "street_or_block", nullable = false, length = 30)
    private String streetOrBlock;

    @Column(name = "ownership_ratio", precision = 5, scale = 2)
    private BigDecimal ownershipRatio;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected House() {
        // JPA constructor
    }

    public House(String houseNumber, String streetOrBlock, BigDecimal ownershipRatio) {
        this.houseNumber = houseNumber;
        this.streetOrBlock = streetOrBlock;
        this.ownershipRatio = ownershipRatio != null ? ownershipRatio : BigDecimal.ONE;
        this.createdAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public String getStreetOrBlock() {
        return streetOrBlock;
    }

    public BigDecimal getOwnershipRatio() {
        return ownershipRatio;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
