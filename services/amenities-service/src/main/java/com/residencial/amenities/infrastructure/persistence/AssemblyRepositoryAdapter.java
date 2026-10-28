package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.Assembly;
import com.residencial.amenities.domain.repository.AssemblyRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class AssemblyRepositoryAdapter implements AssemblyRepository {

    private final JpaAssemblyRepository jpaAssemblyRepository;

    public AssemblyRepositoryAdapter(JpaAssemblyRepository jpaAssemblyRepository) {
        this.jpaAssemblyRepository = jpaAssemblyRepository;
    }

    @Override
    public Assembly save(Assembly assembly) {
        return jpaAssemblyRepository.save(assembly);
    }

    @Override
    public Optional<Assembly> findById(UUID id) {
        return jpaAssemblyRepository.findById(id);
    }

    @Override
    public List<Assembly> findAll() {
        return jpaAssemblyRepository.findAll();
    }
}
