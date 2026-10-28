package com.residencial.parking.infrastructure.persistence;

import com.residencial.parking.domain.model.ParkingFine;
import com.residencial.parking.domain.repository.ParkingFineRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class ParkingFineRepositoryAdapter implements ParkingFineRepository {

    private final JpaParkingFineRepository jpaFineRepository;

    public ParkingFineRepositoryAdapter(JpaParkingFineRepository jpaFineRepository) {
        this.jpaFineRepository = jpaFineRepository;
    }

    @Override
    public ParkingFine save(ParkingFine fine) {
        return jpaFineRepository.save(fine);
    }

    @Override
    public Optional<ParkingFine> findById(UUID id) {
        return jpaFineRepository.findById(id);
    }

    @Override
    public List<ParkingFine> findByPlate(String plate) {
        return jpaFineRepository.findByPlateOrderByCreatedAtDesc(plate.toUpperCase().trim());
    }

    @Override
    public List<ParkingFine> findByHouseId(UUID houseId) {
        return jpaFineRepository.findByHouseIdOrderByCreatedAtDesc(houseId);
    }

    @Override
    public List<ParkingFine> findAll() {
        return jpaFineRepository.findAllByOrderByCreatedAtDesc();
    }
}
