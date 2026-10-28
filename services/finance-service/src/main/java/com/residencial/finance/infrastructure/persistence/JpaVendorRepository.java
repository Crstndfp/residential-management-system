package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaVendorRepository extends JpaRepository<Vendor, UUID> {
    Optional<Vendor> findByTaxId(String taxId);
}
