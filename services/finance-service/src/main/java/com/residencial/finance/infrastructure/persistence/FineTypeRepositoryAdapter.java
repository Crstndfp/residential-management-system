package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.FineType;
import com.residencial.finance.domain.repository.FineTypeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class FineTypeRepositoryAdapter implements FineTypeRepository {

    private final JpaFineTypeRepository jpaFineTypeRepository;

    public FineTypeRepositoryAdapter(JpaFineTypeRepository jpaFineTypeRepository) {
        this.jpaFineTypeRepository = jpaFineTypeRepository;
    }

    @Override
    public FineType save(FineType fineType) {
        return jpaFineTypeRepository.save(fineType);
    }

    @Override
    public Optional<FineType> findById(UUID id) {
        return jpaFineTypeRepository.findById(id);
    }

    @Override
    public Optional<FineType> findByCode(String code) {
        return jpaFineTypeRepository.findByCode(code);
    }
}
