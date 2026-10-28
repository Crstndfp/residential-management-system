package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.MaintenanceCharge;
import com.residencial.finance.domain.repository.MaintenanceChargeRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class MaintenanceChargeRepositoryAdapter implements MaintenanceChargeRepository {

    private final JpaMaintenanceChargeRepository jpaMaintenanceChargeRepository;

    public MaintenanceChargeRepositoryAdapter(JpaMaintenanceChargeRepository jpaMaintenanceChargeRepository) {
        this.jpaMaintenanceChargeRepository = jpaMaintenanceChargeRepository;
    }

    @Override
    public MaintenanceCharge save(MaintenanceCharge charge) {
        return jpaMaintenanceChargeRepository.save(charge);
    }

    @Override
    public Optional<MaintenanceCharge> findById(UUID id) {
        return jpaMaintenanceChargeRepository.findById(id);
    }

    @Override
    public Optional<MaintenanceCharge> findPendingByHouseNumber(String houseNumber) {
        return jpaMaintenanceChargeRepository.findFirstPendingByHouseNumber(houseNumber);
    }

    @Override
    public java.util.List<MaintenanceCharge> findByHouseId(UUID houseId) {
        return jpaMaintenanceChargeRepository.findByHouseId(houseId);
    }
}
