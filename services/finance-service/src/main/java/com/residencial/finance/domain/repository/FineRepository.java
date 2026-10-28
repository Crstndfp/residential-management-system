package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.Fine;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FineRepository {
    Fine save(Fine fine);
    Optional<Fine> findById(UUID id);
    List<Fine> findByHouseId(UUID houseId);
}
