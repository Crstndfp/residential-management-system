package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.CalendarSlotDto;
import com.residencial.amenities.domain.exception.AmenityNotFoundException;
import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.model.Booking;
import com.residencial.amenities.domain.repository.AmenityRepository;
import com.residencial.amenities.domain.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class GetAmenityCalendarUseCase {

    private final AmenityRepository amenityRepository;
    private final BookingRepository bookingRepository;

    public GetAmenityCalendarUseCase(AmenityRepository amenityRepository,
                                     BookingRepository bookingRepository) {
        this.amenityRepository = amenityRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional(readOnly = true)
    public List<CalendarSlotDto> execute(UUID amenityId, LocalDate startDate, LocalDate endDate) {
        if (amenityId == null) {
            throw new IllegalArgumentException("Amenity ID cannot be null.");
        }
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date cannot be null.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before start date.");
        }

        Amenity amenity = amenityRepository.findById(amenityId)
                .orElseThrow(() -> new AmenityNotFoundException("Amenity with ID '" + amenityId + "' was not found."));

        List<Booking> bookings = bookingRepository.findByAmenityIdAndBookingDateBetween(amenity.getId(), startDate, endDate);

        // US-302 Privacy Rule: Occupied slots show strictly "Reservado" with no resident names or event types
        return bookings.stream()
                .map(b -> new CalendarSlotDto(
                        b.getBookingDate(),
                        b.getStartTime(),
                        b.getEndTime(),
                        "Reservado"
                ))
                .toList();
    }
}
