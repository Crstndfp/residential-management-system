package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.Amenity;
import com.residencial.amenities.domain.repository.AmenityRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AmenityRepositoryAdapter implements AmenityRepository {

    private final JpaAmenityRepository jpaAmenityRepository;

    public AmenityRepositoryAdapter(JpaAmenityRepository jpaAmenityRepository) {
        this.jpaAmenityRepository = jpaAmenityRepository;
    }

    @Override
    public Amenity save(Amenity amenity) {
        return jpaAmenityRepository.save(amenity);
    }

    @Override
    public Optional<Amenity> findById(UUID id) {
        return jpaAmenityRepository.findById(id);
    }

    @Override
    public Optional<Amenity> findByCode(String code) {
        return jpaAmenityRepository.findByCode(code);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaAmenityRepository.existsByCode(code);
    }

    @Override
    public List<Amenity> findAllActive() {
        return jpaAmenityRepository.findByIsActiveTrue();
    }

    @Override
    public List<Amenity> findAll() {
        return jpaAmenityRepository.findAll();
    }
}
