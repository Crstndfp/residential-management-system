package com.residencial.parking.infrastructure.persistence;

import com.residencial.parking.domain.model.ParkingFine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaParkingFineRepository extends JpaRepository<ParkingFine, UUID> {
    List<ParkingFine> findByPlateOrderByCreatedAtDesc(String plate);
    List<ParkingFine> findByHouseIdOrderByCreatedAtDesc(UUID houseId);
    List<ParkingFine> findAllByOrderByCreatedAtDesc();
}
