package com.residencial.parking.domain.model;

import jakarta.persistence.*;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

@Entity
@Table(name = "vehicle_entries", schema = "parking_schema")
public class VehicleEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(nullable = false, length = 20)
    private String plate;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Column(name = "visitor_name", nullable = false, length = 150)
    private String visitorName;

    @Column(name = "visitor_id_document", nullable = false, length = 50)
    private String visitorIdDocument;

    @Column(name = "parking_spot_id")
    private UUID parkingSpotId;

    @Column(name = "spot_number", nullable = false, length = 20)
    private String spotNumber;

    @Column(name = "guard_user_id", nullable = false)
    private UUID guardUserId;

    @Column(name = "entry_time", nullable = false)
    private Instant entryTime;

    @Column(name = "exit_time")
    private Instant exitTime;

    @Column(name = "duration_minutes")
    private Long durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private VehicleEntryStatus status;

    @Column(name = "is_overnight", nullable = false)
    private boolean isOvernight;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected VehicleEntry() {
    }

    public VehicleEntry(UUID id, String plate, UUID houseId, String houseNumber,
                        String visitorName, String visitorIdDocument, UUID parkingSpotId,
                        String spotNumber, UUID guardUserId, Instant entryTime,
                        Instant exitTime, Long durationMinutes, VehicleEntryStatus status,
                        boolean isOvernight, Instant createdAt, Instant updatedAt) {
        if (plate == null || plate.isBlank()) {
            throw new IllegalArgumentException("License plate cannot be blank");
        }
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null");
        }
        if (houseNumber == null || houseNumber.isBlank()) {
            throw new IllegalArgumentException("House number cannot be blank");
        }
        if (visitorName == null || visitorName.isBlank()) {
            throw new IllegalArgumentException("Visitor name cannot be blank");
        }
        if (visitorIdDocument == null || visitorIdDocument.isBlank()) {
            throw new IllegalArgumentException("Visitor ID document cannot be blank");
        }
        if (spotNumber == null || spotNumber.isBlank()) {
            throw new IllegalArgumentException("Spot number cannot be blank");
        }
        if (guardUserId == null) {
            throw new IllegalArgumentException("Guard user ID cannot be null");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.plate = plate.toUpperCase().trim();
        this.houseId = houseId;
        this.houseNumber = houseNumber;
        this.visitorName = visitorName;
        this.visitorIdDocument = visitorIdDocument;
        this.parkingSpotId = parkingSpotId;
        this.spotNumber = spotNumber;
        this.guardUserId = guardUserId;
        this.entryTime = entryTime != null ? entryTime : Instant.now();
        this.exitTime = exitTime;
        this.durationMinutes = durationMinutes;
        this.status = status != null ? status : VehicleEntryStatus.ACTIVE;
        this.isOvernight = isOvernight;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.updatedAt = updatedAt != null ? updatedAt : this.createdAt;
    }

    public void recordExit(Instant exitTimestamp, ZoneId zoneId) {
        if (this.status == VehicleEntryStatus.COMPLETED) {
            throw new IllegalStateException("Vehicle entry has already been completed");
        }
        Instant effectiveExit = exitTimestamp != null ? exitTimestamp : Instant.now();
        if (effectiveExit.isBefore(this.entryTime)) {
            throw new IllegalArgumentException("Exit time cannot be earlier than entry time");
        }

        this.exitTime = effectiveExit;
        this.durationMinutes = Duration.between(this.entryTime, this.exitTime).toMinutes();
        this.status = VehicleEntryStatus.COMPLETED;
        this.isOvernight = evaluateOvernightStay(this.entryTime, this.exitTime, zoneId != null ? zoneId : ZoneId.systemDefault());
        this.updatedAt = Instant.now();
    }

    public static boolean evaluateOvernightStay(Instant entry, Instant exit, ZoneId zone) {
        if (entry == null || exit == null) {
            return false;
        }
        ZonedDateTime zEntry = entry.atZone(zone);
        ZonedDateTime zExit = exit.atZone(zone);

        int entryHour = zEntry.getHour();
        int exitHour = zExit.getHour();

        // Entered during evening (20:00 to 23:59) and left morning (05:00 to 08:59)
        boolean enteredEvening = entryHour >= 20 && entryHour <= 23;
        boolean exitedMorning = exitHour >= 5 && exitHour <= 8;
        boolean differentDayOrLongStay = Duration.between(entry, exit).toHours() >= 5;

        return (enteredEvening && exitedMorning && differentDayOrLongStay)
                || Duration.between(entry, exit).toHours() >= 10;
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

    public String getVisitorName() {
        return visitorName;
    }

    public String getVisitorIdDocument() {
        return visitorIdDocument;
    }

    public UUID getParkingSpotId() {
        return parkingSpotId;
    }

    public String getSpotNumber() {
        return spotNumber;
    }

    public UUID getGuardUserId() {
        return guardUserId;
    }

    public Instant getEntryTime() {
        return entryTime;
    }

    public Instant getExitTime() {
        return exitTime;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public VehicleEntryStatus getStatus() {
        return status;
    }

    public boolean isOvernight() {
        return isOvernight;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
