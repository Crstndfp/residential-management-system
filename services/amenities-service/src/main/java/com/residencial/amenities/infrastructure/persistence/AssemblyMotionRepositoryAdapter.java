package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.AssemblyMotion;
import com.residencial.amenities.domain.repository.AssemblyMotionRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AssemblyMotionRepositoryAdapter implements AssemblyMotionRepository {

    private final JpaAssemblyMotionRepository jpaMotionRepository;

    public AssemblyMotionRepositoryAdapter(JpaAssemblyMotionRepository jpaMotionRepository) {
        this.jpaMotionRepository = jpaMotionRepository;
    }

    @Override
    public AssemblyMotion save(AssemblyMotion motion) {
        return jpaMotionRepository.save(motion);
    }

    @Override
    public Optional<AssemblyMotion> findById(UUID id) {
        return jpaMotionRepository.findById(id);
    }

    @Override
    public List<AssemblyMotion> findByAssemblyId(UUID assemblyId) {
        return jpaMotionRepository.findByAssemblyId(assemblyId);
    }
}
