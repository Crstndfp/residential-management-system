package com.residencial.identity.domain.model;

import jakarta.persistence.*;
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

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected House() {
        // JPA constructor
    }

    public House(String houseNumber, String streetOrBlock) {
        if (houseNumber == null || houseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("House number cannot be empty.");
        }
        if (streetOrBlock == null || streetOrBlock.trim().isEmpty()) {
            throw new IllegalArgumentException("Street or block cannot be empty.");
        }
        this.houseNumber = houseNumber.trim();
        this.streetOrBlock = streetOrBlock.trim();
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

    public Instant getCreatedAt() {
        return createdAt;
    }
}
