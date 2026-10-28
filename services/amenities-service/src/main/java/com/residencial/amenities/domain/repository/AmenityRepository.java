package com.residencial.amenities.domain.repository;

import com.residencial.amenities.domain.model.Amenity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AmenityRepository {
    Amenity save(Amenity amenity);
    Optional<Amenity> findById(UUID id);
    Optional<Amenity> findByCode(String code);
    boolean existsByCode(String code);
    List<Amenity> findAllActive();
    List<Amenity> findAll();
}
