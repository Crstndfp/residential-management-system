package com.residencial.parking.infrastructure.persistence;

import com.residencial.parking.domain.model.ParkingSpot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaParkingSpotRepository extends JpaRepository<ParkingSpot, UUID> {
    Optional<ParkingSpot> findBySpotNumber(String spotNumber);

    @Query("SELECT p FROM ParkingSpot p WHERE p.isOccupied = false AND p.spotType = 'VISITOR' ORDER BY p.spotNumber ASC LIMIT 1")
    Optional<ParkingSpot> findFirstAvailableVisitorSpot();

    List<ParkingSpot> findAllByOrderBySpotNumberAsc();

    long countByIsOccupiedFalse();
    long countByIsOccupiedTrue();
}
