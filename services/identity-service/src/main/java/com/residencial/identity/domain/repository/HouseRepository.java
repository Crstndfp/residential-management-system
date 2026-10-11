package com.residencial.identity.domain.repository;

import com.residencial.identity.domain.model.House;
import java.util.Optional;
import java.util.UUID;

public interface HouseRepository {
    House save(House house);
    Optional<House> findById(UUID id);
    Optional<House> findByHouseNumber(String houseNumber);
    boolean existsByHouseNumber(String houseNumber);
}
