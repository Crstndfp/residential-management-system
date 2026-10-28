package com.residencial.finance.application.usecase;

import com.residencial.finance.application.dto.RegisterInvoiceRequest;
import com.residencial.finance.application.dto.RegisterInvoiceResponse;
import com.residencial.finance.domain.exception.CategoryNotFoundException;
import com.residencial.finance.domain.exception.DuplicateInvoiceException;
import com.residencial.finance.domain.model.ExpenseCategory;
import com.residencial.finance.domain.model.Invoice;
import com.residencial.finance.domain.model.InvoiceStatus;
import com.residencial.finance.domain.model.Vendor;
import com.residencial.finance.domain.repository.ExpenseCategoryRepository;
import com.residencial.finance.domain.repository.InvoiceRepository;
import com.residencial.finance.domain.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterExpenseInvoiceUseCaseTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private ExpenseCategoryRepository expenseCategoryRepository;

    private RegisterExpenseInvoiceUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new RegisterExpenseInvoiceUseCase(invoiceRepository, vendorRepository, expenseCategoryRepository);
    }

    @Test
    @DisplayName("US-201: Successfully register expense invoice with vendor resolution")
    void shouldRegisterInvoiceSuccessfully() {
        RegisterInvoiceRequest request = new RegisterInvoiceRequest(
                "FAC-2026-001",
                "9876543-2",
                "Seguridad Total S.A.",
                "ventas@seguridadtotal.com",
                "2333-4455",
                "SECURITY",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                new BigDecimal("12500.00"),
                "https://storage.example.com/invoices/fac-001.pdf",
                "Servicio de garita y rondas nocturnas"
        );

        Vendor vendor = new Vendor("9876543-2", "Seguridad Total S.A.", "ventas@seguridadtotal.com", "2333-4455");
        ExpenseCategory category = new ExpenseCategory("SECURITY", "Seguridad Privada", "Vigilancia");

        when(vendorRepository.findByTaxId("9876543-2")).thenReturn(Optional.of(vendor));
        when(invoiceRepository.existsByVendorIdAndInvoiceNumber(vendor.getId(), "FAC-2026-001")).thenReturn(false);
        when(expenseCategoryRepository.findByCode("SECURITY")).thenReturn(Optional.of(category));
        when(invoiceRepository.save(any(Invoice.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegisterInvoiceResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("FAC-2026-001", response.invoiceNumber());
        assertEquals("Seguridad Total S.A.", response.vendorName());
        assertEquals("SECURITY", response.categoryCode());
        assertEquals(new BigDecimal("12500.00"), response.amount());
        assertEquals(InvoiceStatus.PENDING, response.status());

        verify(invoiceRepository).save(any(Invoice.class));
    }

    @Test
    @DisplayName("US-201: Reject duplicate invoice number for the same vendor")
    void shouldRejectDuplicateInvoiceForSameVendor() {
        RegisterInvoiceRequest request = new RegisterInvoiceRequest(
                "FAC-2026-001",
                "9876543-2",
                "Seguridad Total S.A.",
                null, null,
                "SECURITY",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                new BigDecimal("12500.00"),
                null, null
        );

        Vendor vendor = new Vendor("9876543-2", "Seguridad Total S.A.", null, null);
        when(vendorRepository.findByTaxId("9876543-2")).thenReturn(Optional.of(vendor));
        when(invoiceRepository.existsByVendorIdAndInvoiceNumber(vendor.getId(), "FAC-2026-001")).thenReturn(true);

        DuplicateInvoiceException ex = assertThrows(
                DuplicateInvoiceException.class,
                () -> useCase.execute(request)
        );

        assertEquals("DUPLICATE_INVOICE", ex.getErrorCode());
        verify(invoiceRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-201: Reject invoice when category code does not exist")
    void shouldRejectWhenCategoryNotFound() {
        RegisterInvoiceRequest request = new RegisterInvoiceRequest(
                "FAC-2026-002",
                "9876543-2",
                "Seguridad Total S.A.",
                null, null,
                "NON_EXISTENT_CAT",
                LocalDate.of(2026, 10, 1),
                LocalDate.of(2026, 10, 31),
                new BigDecimal("500.00"),
                null, null
        );

        Vendor vendor = new Vendor("9876543-2", "Seguridad Total S.A.", null, null);
        when(vendorRepository.findByTaxId("9876543-2")).thenReturn(Optional.of(vendor));
        when(invoiceRepository.existsByVendorIdAndInvoiceNumber(vendor.getId(), "FAC-2026-002")).thenReturn(false);
        when(expenseCategoryRepository.findByCode("NON_EXISTENT_CAT")).thenReturn(Optional.empty());

        assertThrows(CategoryNotFoundException.class, () -> useCase.execute(request));
        verify(invoiceRepository, never()).save(any());
    }
}
