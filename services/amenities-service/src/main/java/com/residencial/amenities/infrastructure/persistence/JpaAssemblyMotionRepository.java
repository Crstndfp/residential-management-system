package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.AssemblyMotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaAssemblyMotionRepository extends JpaRepository<AssemblyMotion, UUID> {
    List<AssemblyMotion> findByAssemblyId(UUID assemblyId);
}
