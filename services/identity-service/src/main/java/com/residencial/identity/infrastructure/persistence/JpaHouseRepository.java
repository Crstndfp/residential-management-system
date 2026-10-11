package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.House;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaHouseRepository extends JpaRepository<House, UUID> {
    Optional<House> findByHouseNumber(String houseNumber);
    boolean existsByHouseNumber(String houseNumber);
}
