package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.BookingResponse;
import com.residencial.amenities.application.dto.CreateBookingRequest;
import com.residencial.amenities.domain.exception.AmenityNotFoundException;
import com.residencial.amenities.domain.exception.BookingConflictException;
import com.residencial.amenities.domain.exception.DelinquentHouseException;
import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.model.Booking;
import com.residencial.amenities.domain.port.HouseSolvencyPort;
import com.residencial.amenities.domain.repository.AmenityRepository;
import com.residencial.amenities.domain.repository.BookingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CreateBookingUseCase {

    private final AmenityRepository amenityRepository;
    private final BookingRepository bookingRepository;
    private final HouseSolvencyPort houseSolvencyPort;

    public CreateBookingUseCase(AmenityRepository amenityRepository,
                                BookingRepository bookingRepository,
                                HouseSolvencyPort houseSolvencyPort) {
        this.amenityRepository = amenityRepository;
        this.bookingRepository = bookingRepository;
        this.houseSolvencyPort = houseSolvencyPort;
    }

    @Transactional
    public BookingResponse execute(CreateBookingRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Booking request cannot be null.");
        }

        Amenity amenity = amenityRepository.findById(request.amenityId())
                .orElseThrow(() -> new AmenityNotFoundException("Amenity with ID '" + request.amenityId() + "' was not found."));

        if (!amenity.isActive()) {
            throw new IllegalStateException("Amenity '" + amenity.getName() + "' is temporarily inactive or under maintenance.");
        }

        // US-302 Acceptance Criteria: Solvency Validation
        boolean isSolvent = houseSolvencyPort.isHouseSolvent(request.houseId());
        if (!isSolvent) {
            throw new DelinquentHouseException(request.houseNumber());
        }

        // US-302 Invariant: Slot availability and overlap prevention
        List<Booking> existingBookings = bookingRepository.findByAmenityIdAndBookingDate(amenity.getId(), request.bookingDate());
        for (Booking existing : existingBookings) {
            if (existing.overlapsWith(request.bookingDate(), request.startTime(), request.endTime())) {
                throw new BookingConflictException("The selected time slot (" + request.startTime() + " - " + request.endTime()
                        + ") is already booked on date " + request.bookingDate() + ".");
            }
        }

        // Automatic billing: In a microservices architecture, a financial charge reference is linked
        UUID financeChargeId = UUID.randomUUID();

        Booking booking = new Booking(
                amenity,
                request.houseId(),
                request.houseNumber(),
                request.residentUserId(),
                request.residentName(),
                request.eventType(),
                request.bookingDate(),
                request.startTime(),
                request.endTime(),
                amenity.getRentalCost(),
                amenity.getDepositCost(),
                financeChargeId
        );

        Booking saved = bookingRepository.save(booking);

        return new BookingResponse(
                saved.getId(),
                saved.getAmenity().getId(),
                saved.getAmenity().getName(),
                saved.getHouseId(),
                saved.getHouseNumber(),
                saved.getResidentUserId(),
                saved.getResidentName(),
                saved.getEventType(),
                saved.getBookingDate(),
                saved.getStartTime(),
                saved.getEndTime(),
                saved.getStatus(),
                saved.getTotalCost(),
                saved.getDepositAmount(),
                saved.getFinanceChargeId(),
                saved.getCreatedAt()
        );
    }
}
