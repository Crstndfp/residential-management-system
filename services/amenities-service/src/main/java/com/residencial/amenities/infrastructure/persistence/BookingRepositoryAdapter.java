package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.Booking;
import com.residencial.amenities.domain.repository.BookingRepository;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BookingRepositoryAdapter implements BookingRepository {

    private final JpaBookingRepository jpaBookingRepository;

    public BookingRepositoryAdapter(JpaBookingRepository jpaBookingRepository) {
        this.jpaBookingRepository = jpaBookingRepository;
    }

    @Override
    public Booking save(Booking booking) {
        return jpaBookingRepository.save(booking);
    }

    @Override
    public Optional<Booking> findById(UUID id) {
        return jpaBookingRepository.findById(id);
    }

    @Override
    public List<Booking> findByAmenityIdAndBookingDate(UUID amenityId, LocalDate bookingDate) {
        return jpaBookingRepository.findActiveByAmenityAndDate(amenityId, bookingDate);
    }

    @Override
    public List<Booking> findByAmenityIdAndBookingDateBetween(UUID amenityId, LocalDate startDate, LocalDate endDate) {
        return jpaBookingRepository.findActiveByAmenityAndDateBetween(amenityId, startDate, endDate);
    }

    @Override
    public List<Booking> findByHouseId(UUID houseId) {
        return jpaBookingRepository.findByHouseIdOrderByBookingDateDesc(houseId);
    }
}
