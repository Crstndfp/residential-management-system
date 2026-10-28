package com.residencial.amenities.domain.repository;

import com.residencial.amenities.domain.model.Booking;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BookingRepository {
    Booking save(Booking booking);
    Optional<Booking> findById(UUID id);
    List<Booking> findByAmenityIdAndBookingDate(UUID amenityId, LocalDate bookingDate);
    List<Booking> findByAmenityIdAndBookingDateBetween(UUID amenityId, LocalDate startDate, LocalDate endDate);
    List<Booking> findByHouseId(UUID houseId);
}
