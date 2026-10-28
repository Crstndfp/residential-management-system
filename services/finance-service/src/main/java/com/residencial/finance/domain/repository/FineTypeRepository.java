package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.FineType;
import java.util.Optional;
import java.util.UUID;

public interface FineTypeRepository {
    FineType save(FineType fineType);
    Optional<FineType> findById(UUID id);
    Optional<FineType> findByCode(String code);
}
