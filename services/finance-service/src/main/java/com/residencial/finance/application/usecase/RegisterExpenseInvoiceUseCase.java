package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.RegisterInvoiceRequest;
import com.residencial.finance.application.dto.RegisterInvoiceResponse;
import com.residencial.finance.domain.exception.CategoryNotFoundException;
import com.residencial.finance.domain.exception.DuplicateInvoiceException;
import com.residencial.finance.domain.model.ExpenseCategory;
import com.residencial.finance.domain.model.Invoice;
import com.residencial.finance.domain.model.Vendor;
import com.residencial.finance.domain.repository.ExpenseCategoryRepository;
import com.residencial.finance.domain.repository.InvoiceRepository;
import com.residencial.finance.domain.repository.VendorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RegisterExpenseInvoiceUseCase {

    private final InvoiceRepository invoiceRepository;
    private final VendorRepository vendorRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    public RegisterExpenseInvoiceUseCase(
            InvoiceRepository invoiceRepository,
            VendorRepository vendorRepository,
            ExpenseCategoryRepository expenseCategoryRepository) {
        this.invoiceRepository = invoiceRepository;
        this.vendorRepository = vendorRepository;
        this.expenseCategoryRepository = expenseCategoryRepository;
    }

    public RegisterInvoiceResponse execute(RegisterInvoiceRequest request) {
        // 1. Resolve or create Vendor by Tax ID
        String taxId = request.vendorTaxId().trim().toUpperCase();
        Vendor vendor = vendorRepository.findByTaxId(taxId)
                .orElseGet(() -> vendorRepository.save(new Vendor(
                        taxId,
                        request.vendorName(),
                        request.vendorEmail(),
                        request.vendorPhone()
                )));

        // 2. Enforce unique invoice number per vendor
        String invoiceNumber = request.invoiceNumber().trim();
        if (invoiceRepository.existsByVendorIdAndInvoiceNumber(vendor.getId(), invoiceNumber)) {
            throw new DuplicateInvoiceException(invoiceNumber, vendor.getTaxId());
        }

        // 3. Resolve active Expense Category
        String categoryCode = request.categoryCode().trim().toUpperCase();
        ExpenseCategory category = expenseCategoryRepository.findByCode(categoryCode)
                .filter(ExpenseCategory::isActive)
                .orElseThrow(() -> new CategoryNotFoundException(categoryCode));

        // 4. Instantiate and validate Invoice Domain Aggregate
        Invoice invoice = new Invoice(
                invoiceNumber,
                vendor,
                category,
                request.issueDate(),
                request.dueDate(),
                request.amount(),
                request.documentUrl(),
                request.notes()
        );

        Invoice savedInvoice = invoiceRepository.save(invoice);

        return new RegisterInvoiceResponse(
                savedInvoice.getId(),
                savedInvoice.getInvoiceNumber(),
                vendor.getId(),
                vendor.getName(),
                vendor.getTaxId(),
                category.getCode(),
                category.getName(),
                savedInvoice.getIssueDate(),
                savedInvoice.getDueDate(),
                savedInvoice.getAmount(),
                savedInvoice.getStatus(),
                savedInvoice.getDocumentUrl(),
                savedInvoice.getCreatedAt()
        );
    }
}
