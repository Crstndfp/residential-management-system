package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface JpaBookingRepository extends JpaRepository<Booking, UUID> {

    @Query("SELECT b FROM Booking b WHERE b.amenity.id = :amenityId AND b.bookingDate = :bookingDate AND b.status <> 'CANCELLED'")
    List<Booking> findActiveByAmenityAndDate(@Param("amenityId") UUID amenityId,
                                            @Param("bookingDate") LocalDate bookingDate);

    @Query("SELECT b FROM Booking b WHERE b.amenity.id = :amenityId AND b.bookingDate BETWEEN :startDate AND :endDate AND b.status <> 'CANCELLED' ORDER BY b.bookingDate ASC, b.startTime ASC")
    List<Booking> findActiveByAmenityAndDateBetween(@Param("amenityId") UUID amenityId,
                                                   @Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);

    List<Booking> findByHouseIdOrderByBookingDateDesc(UUID houseId);
}
