package com.residencial.parking.domain.repository;

import com.residencial.parking.domain.model.ParkingSpot;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingSpotRepository {
    ParkingSpot save(ParkingSpot spot);
    Optional<ParkingSpot> findById(UUID id);
    Optional<ParkingSpot> findBySpotNumber(String spotNumber);
    Optional<ParkingSpot> findFirstAvailableVisitorSpot();
    List<ParkingSpot> findAllOrderBySpotNumber();
    long countAvailableSpots();
    long countOccupiedSpots();
}
