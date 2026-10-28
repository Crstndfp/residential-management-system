package com.residencial.amenities.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AmenitiesDomainTest {

    @Test
    @DisplayName("Amenity Invariant: Valid construction, uppercase code normalization and event check")
    void testAmenityValidConstruction() {
        Amenity amenity = new Amenity(
                "  club_house  ",
                "  Casa Club  ",
                "Salon de fiestas",
                100,
                new BigDecimal("500.00"),
                new BigDecimal("300.00"),
                LocalTime.of(8, 0),
                LocalTime.of(23, 0),
                "birthday, wedding, family_gathering"
        );

        assertNotNull(amenity.getId());
        assertEquals("CLUB_HOUSE", amenity.getCode());
        assertEquals("Casa Club", amenity.getName());
        assertEquals(100, amenity.getCapacity());
        assertTrue(amenity.isActive());

        assertTrue(amenity.isEventTypeAllowed("BIRTHDAY"));
        assertTrue(amenity.isEventTypeAllowed("wedding"));
        assertFalse(amenity.isEventTypeAllowed("SPORTS_MATCH"));

        assertTrue(amenity.isTimeWithinOpeningHours(LocalTime.of(10, 0), LocalTime.of(15, 0)));
        assertFalse(amenity.isTimeWithinOpeningHours(LocalTime.of(6, 0), LocalTime.of(10, 0))); // Before 08:00
        assertFalse(amenity.isTimeWithinOpeningHours(LocalTime.of(20, 0), LocalTime.of(23, 30))); // After 23:00
    }

    @Test
    @DisplayName("Amenity Invariant: Validation errors on invalid parameters")
    void testAmenityValidationErrors() {
        LocalTime open = LocalTime.of(8, 0);
        LocalTime close = LocalTime.of(20, 0);

        assertThrows(IllegalArgumentException.class, () -> new Amenity(null, "Name", null, 10, BigDecimal.ZERO, BigDecimal.ZERO, open, close, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("   ", "Name", null, 10, BigDecimal.ZERO, BigDecimal.ZERO, open, close, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", null, null, 10, BigDecimal.ZERO, BigDecimal.ZERO, open, close, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "  ", null, 10, BigDecimal.ZERO, BigDecimal.ZERO, open, close, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, 0, BigDecimal.ZERO, BigDecimal.ZERO, open, close, "PARTY")); // 0 capacity
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, -5, BigDecimal.ZERO, BigDecimal.ZERO, open, close, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, 10, new BigDecimal("-10"), BigDecimal.ZERO, open, close, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, 10, BigDecimal.ZERO, new BigDecimal("-5"), open, close, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, 10, BigDecimal.ZERO, BigDecimal.ZERO, null, close, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, 10, BigDecimal.ZERO, BigDecimal.ZERO, open, null, "PARTY"));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, 10, BigDecimal.ZERO, BigDecimal.ZERO, open, close, null));
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, 10, BigDecimal.ZERO, BigDecimal.ZERO, close, open, "PARTY")); // close before open
        assertThrows(IllegalArgumentException.class, () -> new Amenity("CODE", "Name", null, 10, BigDecimal.ZERO, BigDecimal.ZERO, open, close, "   "));
    }

    @Test
    @DisplayName("Booking Invariant: Valid construction and overlap detection")
    void testBookingConstructionAndOverlap() {
        Amenity amenity = new Amenity(
                "GAZEBO", "Kiosco", null, 40,
                new BigDecimal("150.00"), new BigDecimal("100.00"),
                LocalTime.of(9, 0), LocalTime.of(21, 0), "BARBECUE"
        );

        LocalDate date = LocalDate.of(2026, 11, 1);
        Booking booking = new Booking(
                amenity,
                UUID.randomUUID(),
                "M-01 L-01",
                UUID.randomUUID(),
                "Ana Morales",
                "BARBECUE",
                date,
                LocalTime.of(12, 0),
                LocalTime.of(16, 0),
                new BigDecimal("150.00"),
                new BigDecimal("100.00"),
                UUID.randomUUID()
        );

        assertNotNull(booking.getId());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
        assertEquals("M-01 L-01", booking.getHouseNumber());

        // Overlap checks
        assertTrue(booking.overlapsWith(date, LocalTime.of(13, 0), LocalTime.of(15, 0))); // Inside
        assertTrue(booking.overlapsWith(date, LocalTime.of(10, 0), LocalTime.of(13, 0))); // Overlaps start
        assertTrue(booking.overlapsWith(date, LocalTime.of(15, 0), LocalTime.of(18, 0))); // Overlaps end
        assertFalse(booking.overlapsWith(date, LocalTime.of(9, 0), LocalTime.of(12, 0)));  // Adjacent before
        assertFalse(booking.overlapsWith(date, LocalTime.of(16, 0), LocalTime.of(20, 0))); // Adjacent after
        assertFalse(booking.overlapsWith(date.plusDays(1), LocalTime.of(12, 0), LocalTime.of(16, 0))); // Different date

        booking.cancel();
        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertFalse(booking.overlapsWith(date, LocalTime.of(13, 0), LocalTime.of(15, 0))); // Cancelled booking never overlaps
    }
}
