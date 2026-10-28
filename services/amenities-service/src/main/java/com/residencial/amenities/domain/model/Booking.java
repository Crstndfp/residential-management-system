package com.residencial.amenities.domain.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "bookings", schema = "amenities_schema")
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "amenity_id", nullable = false)
    private Amenity amenity;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Column(name = "resident_user_id", nullable = false)
    private UUID residentUserId;

    @Column(name = "resident_name", nullable = false, length = 150)
    private String residentName;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalTime endTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BookingStatus status;

    @Column(name = "total_cost", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "deposit_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal depositAmount;

    @Column(name = "finance_charge_id")
    private UUID financeChargeId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Booking() {
        // JPA constructor
    }

    public Booking(Amenity amenity, UUID houseId, String houseNumber,
                   UUID residentUserId, String residentName, String eventType,
                   LocalDate bookingDate, LocalTime startTime, LocalTime endTime,
                   BigDecimal totalCost, BigDecimal depositAmount, UUID financeChargeId) {
        if (amenity == null) {
            throw new IllegalArgumentException("Amenity cannot be null.");
        }
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null.");
        }
        if (houseNumber == null || houseNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("House number cannot be empty.");
        }
        if (residentUserId == null) {
            throw new IllegalArgumentException("Resident user ID cannot be null.");
        }
        if (residentName == null || residentName.trim().isEmpty()) {
            throw new IllegalArgumentException("Resident name cannot be empty.");
        }
        if (eventType == null || eventType.trim().isEmpty()) {
            throw new IllegalArgumentException("Event type cannot be empty.");
        }
        if (bookingDate == null) {
            throw new IllegalArgumentException("Booking date cannot be null.");
        }
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException("Start and end time cannot be null.");
        }
        if (!endTime.isAfter(startTime)) {
            throw new IllegalArgumentException("End time must be after start time.");
        }
        if (!amenity.isEventTypeAllowed(eventType)) {
            throw new IllegalArgumentException("Event type '" + eventType + "' is not permitted for amenity '" + amenity.getName() + "'.");
        }
        if (!amenity.isTimeWithinOpeningHours(startTime, endTime)) {
            throw new IllegalArgumentException("Requested time (" + startTime + " - " + endTime + ") is outside amenity operating hours ("
                    + amenity.getOpeningTime() + " - " + amenity.getClosingTime() + ").");
        }

        this.id = UUID.randomUUID();
        this.amenity = amenity;
        this.houseId = houseId;
        this.houseNumber = houseNumber.trim();
        this.residentUserId = residentUserId;
        this.residentName = residentName.trim();
        this.eventType = eventType.trim().toUpperCase();
        this.bookingDate = bookingDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = BookingStatus.CONFIRMED;
        this.totalCost = totalCost != null ? totalCost : amenity.getRentalCost();
        this.depositAmount = depositAmount != null ? depositAmount : amenity.getDepositCost();
        this.financeChargeId = financeChargeId;
        this.createdAt = Instant.now();
    }

    public void cancel() {
        if (this.status == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Booking is already cancelled.");
        }
        this.status = BookingStatus.CANCELLED;
    }

    public boolean overlapsWith(LocalDate date, LocalTime start, LocalTime end) {
        if (!this.bookingDate.equals(date) || this.status == BookingStatus.CANCELLED) {
            return false;
        }
        return start.isBefore(this.endTime) && end.isAfter(this.startTime);
    }

    public UUID getId() { return id; }
    public Amenity getAmenity() { return amenity; }
    public UUID getHouseId() { return houseId; }
    public String getHouseNumber() { return houseNumber; }
    public UUID getResidentUserId() { return residentUserId; }
    public String getResidentName() { return residentName; }
    public String getEventType() { return eventType; }
    public LocalDate getBookingDate() { return bookingDate; }
    public LocalTime getStartTime() { return startTime; }
    public LocalTime getEndTime() { return endTime; }
    public BookingStatus getStatus() { return status; }
    public BigDecimal getTotalCost() { return totalCost; }
    public BigDecimal getDepositAmount() { return depositAmount; }
    public UUID getFinanceChargeId() { return financeChargeId; }
    public Instant getCreatedAt() { return createdAt; }
}
