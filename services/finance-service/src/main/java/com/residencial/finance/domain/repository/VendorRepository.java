package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.Vendor;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepository {
    Vendor save(Vendor vendor);
    Optional<Vendor> findById(UUID id);
    Optional<Vendor> findByTaxId(String taxId);
}
