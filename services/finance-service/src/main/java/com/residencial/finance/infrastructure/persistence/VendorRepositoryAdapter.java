package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.Vendor;
import com.residencial.finance.domain.repository.VendorRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class VendorRepositoryAdapter implements VendorRepository {

    private final JpaVendorRepository jpaVendorRepository;

    public VendorRepositoryAdapter(JpaVendorRepository jpaVendorRepository) {
        this.jpaVendorRepository = jpaVendorRepository;
    }

    @Override
    public Vendor save(Vendor vendor) {
        return jpaVendorRepository.save(vendor);
    }

    @Override
    public Optional<Vendor> findById(UUID id) {
        return jpaVendorRepository.findById(id);
    }

    @Override
    public Optional<Vendor> findByTaxId(String taxId) {
        return jpaVendorRepository.findByTaxId(taxId);
    }
}
