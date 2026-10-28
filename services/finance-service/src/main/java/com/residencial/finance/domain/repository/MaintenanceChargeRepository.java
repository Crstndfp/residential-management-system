package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.MaintenanceCharge;
import java.util.Optional;
import java.util.UUID;

public interface MaintenanceChargeRepository {
    MaintenanceCharge save(MaintenanceCharge charge);
    Optional<MaintenanceCharge> findById(UUID id);
    Optional<MaintenanceCharge> findPendingByHouseNumber(String houseNumber);
}
