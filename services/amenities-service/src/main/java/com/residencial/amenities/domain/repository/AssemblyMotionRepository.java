package com.residencial.amenities.domain.repository;

import com.residencial.amenities.domain.model.AssemblyMotion;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyMotionRepository {
    AssemblyMotion save(AssemblyMotion motion);
    Optional<AssemblyMotion> findById(UUID id);
    List<AssemblyMotion> findByAssemblyId(UUID assemblyId);
}
