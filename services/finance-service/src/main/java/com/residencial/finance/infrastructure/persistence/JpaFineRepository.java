package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.Fine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaFineRepository extends JpaRepository<Fine, UUID> {
    List<Fine> findByHouseId(UUID houseId);
}
