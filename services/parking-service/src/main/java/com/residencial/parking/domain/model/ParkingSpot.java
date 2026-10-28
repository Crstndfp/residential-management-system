package com.residencial.parking.domain.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "parking_spots", schema = "parking_schema")
public class ParkingSpot {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "spot_number", nullable = false, unique = true, length = 20)
    private String spotNumber;

    @Column(name = "spot_type", nullable = false, length = 30)
    private String spotType;

    @Column(name = "is_occupied", nullable = false)
    private boolean isOccupied;

    @Column(name = "current_vehicle_entry_id")
    private UUID currentVehicleEntryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected ParkingSpot() {
    }

    public ParkingSpot(UUID id, String spotNumber, String spotType, boolean isOccupied,
                       UUID currentVehicleEntryId, Instant createdAt, Instant updatedAt) {
        if (spotNumber == null || spotNumber.isBlank()) {
            throw new IllegalArgumentException("Spot number cannot be blank");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.spotNumber = spotNumber;
        this.spotType = spotType != null ? spotType : "VISITOR";
        this.isOccupied = isOccupied;
        this.currentVehicleEntryId = currentVehicleEntryId;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void occupy(UUID entryId) {
        if (this.isOccupied) {
            throw new IllegalStateException("Parking spot " + spotNumber + " is already occupied");
        }
        if (entryId == null) {
            throw new IllegalArgumentException("Vehicle entry ID cannot be null when occupying spot");
        }
        this.isOccupied = true;
        this.currentVehicleEntryId = entryId;
        this.updatedAt = Instant.now();
    }

    public void release() {
        this.isOccupied = false;
        this.currentVehicleEntryId = null;
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public String getSpotType() {
        return spotType;
    }

    public boolean isOccupied() {
        return isOccupied;
    }

    public UUID getCurrentVehicleEntryId() {
        return currentVehicleEntryId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
