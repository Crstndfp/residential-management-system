package com.residencial.parking.domain.repository;

import com.residencial.parking.domain.model.ParkingFine;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParkingFineRepository {
    ParkingFine save(ParkingFine fine);
    Optional<ParkingFine> findById(UUID id);
    List<ParkingFine> findByPlate(String plate);
    List<ParkingFine> findByHouseId(UUID houseId);
    List<ParkingFine> findAll();
}
