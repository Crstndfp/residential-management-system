package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.ChargeStatus;
import com.residencial.finance.domain.model.MaintenanceCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaMaintenanceChargeRepository extends JpaRepository<MaintenanceCharge, UUID> {
    @Query("SELECT c FROM MaintenanceCharge c WHERE c.houseNumber = :houseNumber AND c.status <> 'PAID' ORDER BY c.periodYear ASC, c.periodMonth ASC LIMIT 1")
    Optional<MaintenanceCharge> findFirstPendingByHouseNumber(String houseNumber);

    List<MaintenanceCharge> findByHouseId(UUID houseId);
}
