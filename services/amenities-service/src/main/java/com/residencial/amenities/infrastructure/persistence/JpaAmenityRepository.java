package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaAmenityRepository extends JpaRepository<Amenity, UUID> {
    Optional<Amenity> findByCode(String code);
    boolean existsByCode(String code);
    List<Amenity> findByIsActiveTrue();
}
