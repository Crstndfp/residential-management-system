package com.residencial.amenities.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;

@Entity
@Table(name = "amenities", schema = "amenities_schema")
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "rental_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal rentalCost;

    @Column(name = "deposit_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositCost;

    @Column(name = "opening_time", nullable = false)
    private LocalTime openingTime;

    @Column(name = "closing_time", nullable = false)
    private LocalTime closingTime;

    @Column(name = "allowed_event_types", nullable = false, length = 255)
    private String allowedEventTypes;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Amenity() {
        // JPA constructor
    }

    public Amenity(String code, String name, String description, int capacity,
                   BigDecimal rentalCost, BigDecimal depositCost,
                   LocalTime openingTime, LocalTime closingTime,
                   String allowedEventTypes) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Amenity code cannot be empty.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Amenity name cannot be empty.");
        }
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be strictly greater than zero.");
        }
        if (rentalCost == null || rentalCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Rental cost cannot be negative.");
        }
        if (depositCost == null || depositCost.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Deposit cost cannot be negative.");
        }
        if (openingTime == null || closingTime == null) {
            throw new IllegalArgumentException("Opening and closing times cannot be null.");
        }
        if (!closingTime.isAfter(openingTime)) {
            throw new IllegalArgumentException("Closing time must be after opening time.");
        }
        if (allowedEventTypes == null || allowedEventTypes.trim().isEmpty()) {
            throw new IllegalArgumentException("Allowed event types cannot be empty.");
        }

        this.id = UUID.randomUUID();
        this.code = code.trim().toUpperCase();
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.capacity = capacity;
        this.rentalCost = rentalCost;
        this.depositCost = depositCost;
        this.openingTime = openingTime;
        this.closingTime = closingTime;
        this.allowedEventTypes = allowedEventTypes.trim().toUpperCase();
        this.isActive = true;
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public boolean isEventTypeAllowed(String eventType) {
        if (eventType == null || eventType.trim().isEmpty()) {
            return false;
        }
        String cleanType = eventType.trim().toUpperCase();
        return Arrays.stream(this.allowedEventTypes.split(","))
                .map(String::trim)
                .anyMatch(t -> t.equalsIgnoreCase(cleanType));
    }

    public boolean isTimeWithinOpeningHours(LocalTime start, LocalTime end) {
        if (start == null || end == null || !end.isAfter(start)) {
            return false;
        }
        return !start.isBefore(this.openingTime) && !end.isAfter(this.closingTime);
    }

    public void update(String name, String description, int capacity,
                       BigDecimal rentalCost, BigDecimal depositCost,
                       LocalTime openingTime, LocalTime closingTime,
                       String allowedEventTypes) {
        if (name != null && !name.trim().isEmpty()) this.name = name.trim();
        if (description != null) this.description = description.trim();
        if (capacity > 0) this.capacity = capacity;
        if (rentalCost != null && rentalCost.compareTo(BigDecimal.ZERO) >= 0) this.rentalCost = rentalCost;
        if (depositCost != null && depositCost.compareTo(BigDecimal.ZERO) >= 0) this.depositCost = depositCost;
        if (openingTime != null && closingTime != null && closingTime.isAfter(openingTime)) {
            this.openingTime = openingTime;
            this.closingTime = closingTime;
        }
        if (allowedEventTypes != null && !allowedEventTypes.trim().isEmpty()) {
            this.allowedEventTypes = allowedEventTypes.trim().toUpperCase();
        }
        this.updatedAt = Instant.now();
    }

    public void deactivate() {
        this.isActive = false;
        this.updatedAt = Instant.now();
    }

    public void activate() {
        this.isActive = true;
        this.updatedAt = Instant.now();
    }

    public UUID getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getCapacity() { return capacity; }
    public BigDecimal getRentalCost() { return rentalCost; }
    public BigDecimal getDepositCost() { return depositCost; }
    public LocalTime getOpeningTime() { return openingTime; }
    public LocalTime getClosingTime() { return closingTime; }
    public String getAllowedEventTypes() { return allowedEventTypes; }
    public boolean isActive() { return isActive; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
