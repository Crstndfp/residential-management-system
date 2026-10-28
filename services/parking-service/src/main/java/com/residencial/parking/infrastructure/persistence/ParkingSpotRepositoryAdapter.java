package com.residencial.parking.infrastructure.persistence;

import com.residencial.parking.domain.model.ParkingSpot;
import com.residencial.parking.domain.repository.ParkingSpotRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ParkingSpotRepositoryAdapter implements ParkingSpotRepository {

    private final JpaParkingSpotRepository jpaSpotRepository;

    public ParkingSpotRepositoryAdapter(JpaParkingSpotRepository jpaSpotRepository) {
        this.jpaSpotRepository = jpaSpotRepository;
    }

    @Override
    public ParkingSpot save(ParkingSpot spot) {
        return jpaSpotRepository.save(spot);
    }

    @Override
    public Optional<ParkingSpot> findById(UUID id) {
        return jpaSpotRepository.findById(id);
    }

    @Override
    public Optional<ParkingSpot> findBySpotNumber(String spotNumber) {
        return jpaSpotRepository.findBySpotNumber(spotNumber);
    }

    @Override
    public Optional<ParkingSpot> findFirstAvailableVisitorSpot() {
        return jpaSpotRepository.findFirstAvailableVisitorSpot();
    }

    @Override
    public List<ParkingSpot> findAllOrderBySpotNumber() {
        return jpaSpotRepository.findAllByOrderBySpotNumberAsc();
    }

    @Override
    public long countAvailableSpots() {
        return jpaSpotRepository.countByIsOccupiedFalse();
    }

    @Override
    public long countOccupiedSpots() {
        return jpaSpotRepository.countByIsOccupiedTrue();
    }
}
