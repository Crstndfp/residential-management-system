package com.residencial.amenities.domain.repository;

import com.residencial.amenities.domain.model.Assembly;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyRepository {
    Assembly save(Assembly assembly);
    Optional<Assembly> findById(UUID id);
    List<Assembly> findAll();
}
