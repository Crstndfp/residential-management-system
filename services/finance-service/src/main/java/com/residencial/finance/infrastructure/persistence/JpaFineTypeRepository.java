package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.FineType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaFineTypeRepository extends JpaRepository<FineType, UUID> {
    Optional<FineType> findByCode(String code);
}
