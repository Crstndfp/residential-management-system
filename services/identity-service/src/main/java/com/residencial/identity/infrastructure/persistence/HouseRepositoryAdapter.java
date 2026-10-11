package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.House;
import com.residencial.identity.domain.repository.HouseRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class HouseRepositoryAdapter implements HouseRepository {

    private final JpaHouseRepository jpaHouseRepository;

    public HouseRepositoryAdapter(JpaHouseRepository jpaHouseRepository) {
        this.jpaHouseRepository = jpaHouseRepository;
    }

    @Override
    public House save(House house) {
        return jpaHouseRepository.save(house);
    }

    @Override
    public Optional<House> findById(UUID id) {
        return jpaHouseRepository.findById(id);
    }

    @Override
    public Optional<House> findByHouseNumber(String houseNumber) {
        return jpaHouseRepository.findByHouseNumber(houseNumber);
    }

    @Override
    public boolean existsByHouseNumber(String houseNumber) {
        return jpaHouseRepository.existsByHouseNumber(houseNumber);
    }
}
