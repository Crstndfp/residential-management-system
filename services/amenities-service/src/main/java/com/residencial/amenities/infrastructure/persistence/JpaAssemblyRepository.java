package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.Assembly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaAssemblyRepository extends JpaRepository<Assembly, UUID> {
}
