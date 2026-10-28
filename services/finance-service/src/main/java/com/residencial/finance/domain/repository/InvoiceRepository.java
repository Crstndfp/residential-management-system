package com.residencial.finance.domain.repository;

import com.residencial.finance.domain.model.Invoice;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice invoice);
    Optional<Invoice> findById(UUID id);
    boolean existsByVendorIdAndInvoiceNumber(UUID vendorId, String invoiceNumber);
}
