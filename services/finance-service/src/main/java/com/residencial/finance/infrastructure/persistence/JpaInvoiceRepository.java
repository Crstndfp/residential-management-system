package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface JpaInvoiceRepository extends JpaRepository<Invoice, UUID> {
    boolean existsByVendorIdAndInvoiceNumber(UUID vendorId, String invoiceNumber);
}
