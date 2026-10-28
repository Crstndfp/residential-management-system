package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.Fine;
import com.residencial.finance.domain.repository.FineRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class FineRepositoryAdapter implements FineRepository {

    private final JpaFineRepository jpaFineRepository;

    public FineRepositoryAdapter(JpaFineRepository jpaFineRepository) {
        this.jpaFineRepository = jpaFineRepository;
    }

    @Override
    public Fine save(Fine fine) {
        return jpaFineRepository.save(fine);
    }

    @Override
    public Optional<Fine> findById(UUID id) {
        return jpaFineRepository.findById(id);
    }

    @Override
    public List<Fine> findByHouseId(UUID houseId) {
        return jpaFineRepository.findByHouseId(houseId);
    }
}
