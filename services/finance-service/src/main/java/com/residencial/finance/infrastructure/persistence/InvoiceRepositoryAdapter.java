package com.residencial.finance.infrastructure.persistence;

import com.residencial.finance.domain.model.Invoice;
import com.residencial.finance.domain.repository.InvoiceRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class InvoiceRepositoryAdapter implements InvoiceRepository {

    private final JpaInvoiceRepository jpaInvoiceRepository;

    public InvoiceRepositoryAdapter(JpaInvoiceRepository jpaInvoiceRepository) {
        this.jpaInvoiceRepository = jpaInvoiceRepository;
    }

    @Override
    public Invoice save(Invoice invoice) {
        return jpaInvoiceRepository.save(invoice);
    }

    @Override
    public Optional<Invoice> findById(UUID id) {
        return jpaInvoiceRepository.findById(id);
    }

    @Override
    public boolean existsByVendorIdAndInvoiceNumber(UUID vendorId, String invoiceNumber) {
        return jpaInvoiceRepository.existsByVendorIdAndInvoiceNumber(vendorId, invoiceNumber);
    }
}
