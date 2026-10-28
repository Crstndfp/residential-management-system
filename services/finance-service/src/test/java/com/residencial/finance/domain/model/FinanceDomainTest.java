package com.residencial.finance.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FinanceDomainTest {

    @Test
    @DisplayName("Invoice Invariant: Due date cannot be before issue date")
    void testDueDateCannotBeBeforeIssueDate() {
        Vendor vendor = new Vendor("1234567-8", "CleanCorp", "info@cleancorp.com", "555-1234");
        ExpenseCategory category = new ExpenseCategory("CLEANING", "Limpieza", "Gastos de limpieza");

        LocalDate issue = LocalDate.of(2026, 10, 20);
        LocalDate due = LocalDate.of(2026, 10, 15); // Before issue date

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> new Invoice("FAC-001", vendor, category, issue, due, new BigDecimal("1500.00"), null, null)
        );

        assertTrue(ex.getMessage().contains("Due date cannot be before issue date"));
    }

    @Test
    @DisplayName("Invoice Invariant: Amount must be strictly greater than zero")
    void testAmountMustBeGreaterThanZero() {
        Vendor vendor = new Vendor("1234567-8", "CleanCorp", "info@cleancorp.com", "555-1234");
        ExpenseCategory category = new ExpenseCategory("CLEANING", "Limpieza", "Gastos de limpieza");

        LocalDate issue = LocalDate.of(2026, 10, 20);
        LocalDate due = LocalDate.of(2026, 10, 30);

        assertThrows(
                IllegalArgumentException.class,
                () -> new Invoice("FAC-001", vendor, category, issue, due, BigDecimal.ZERO, null, null)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Invoice("FAC-001", vendor, category, issue, due, new BigDecimal("-10.00"), null, null)
        );
    }

    @Test
    @DisplayName("Invoice Lifecycle: Mark as paid updates status and payment date")
    void testMarkAsPaid() {
        Vendor vendor = new Vendor("1234567-8", "CleanCorp", "info@cleancorp.com", "555-1234");
        ExpenseCategory category = new ExpenseCategory("CLEANING", "Limpieza", "Gastos de limpieza");

        Invoice invoice = new Invoice("FAC-001", vendor, category,
                LocalDate.of(2026, 10, 20), LocalDate.of(2026, 10, 30),
                new BigDecimal("2500.50"), "https://s3.example.com/fac001.pdf", "Mantenimiento bombas");

        assertEquals(InvoiceStatus.PENDING, invoice.getStatus());

        invoice.markAsPaid(LocalDate.of(2026, 10, 25));
        assertEquals(InvoiceStatus.PAID, invoice.getStatus());
        assertEquals(LocalDate.of(2026, 10, 25), invoice.getPaymentDate());
    }

    // =========================================================================
    // US-203 Domain Invariants: Fine & FineType
    // =========================================================================

    @Test
    @DisplayName("Fine Invariant: Valid creation, default vs custom amount, and payment lifecycle")
    void testFineLifecycle() {
        java.util.UUID houseId = java.util.UUID.randomUUID();
        FineType fineType = new FineType("NOISE_VIOLATION", "Ruidos Excesivos", "Musica alta", new BigDecimal("300.00"));

        // Fine with default amount
        Fine fine = new Fine(houseId, "M-05 L-12", fineType, null, "Fiesta en patio", null, null);
        assertNotNull(fine.getId());
        assertEquals(new BigDecimal("300.00"), fine.getAmount());
        assertEquals(FineStatus.PENDING, fine.getStatus());
        assertNull(fine.getPaidDate());

        // Mark as paid
        LocalDate payDate = LocalDate.of(2026, 10, 26);
        fine.markAsPaid(payDate);
        assertEquals(FineStatus.PAID, fine.getStatus());
        assertEquals(payDate, fine.getPaidDate());

        // Cannot dismiss a paid fine
        assertThrows(IllegalStateException.class, () -> fine.dismiss("Justificacion tardia"));
    }

    @Test
    @DisplayName("Fine Invariant: Dismissal lifecycle and restrictions")
    void testFineDismissalLifecycle() {
        java.util.UUID houseId = java.util.UUID.randomUUID();
        FineType fineType = new FineType("PARKING_VIOLATION", "Mal Parqueado", "Bloqueo de rampa", new BigDecimal("200.00"));
        Fine fine = new Fine(houseId, "M-01 L-01", fineType, new BigDecimal("250.00"), "Bloqueo de acera", null, null);

        assertEquals(new BigDecimal("250.00"), fine.getAmount());

        fine.dismiss("Error de guardia de garita al anotar placa");
        assertEquals(FineStatus.DISMISSED, fine.getStatus());
        assertTrue(fine.getReason().contains("Error de guardia"));

        // Cannot pay a dismissed fine
        assertThrows(IllegalStateException.class, () -> fine.markAsPaid(LocalDate.now()));
    }

    @Test
    @DisplayName("Fine Invariant: Validation errors on missing required fields")
    void testFineValidation() {
        java.util.UUID houseId = java.util.UUID.randomUUID();
        FineType fineType = new FineType("SPEEDING", "Velocidad", "Exceso", new BigDecimal("500.00"));

        assertThrows(IllegalArgumentException.class, () -> new Fine(null, "M-01", fineType, null, "Motivo", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Fine(houseId, "   ", fineType, null, "Motivo", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Fine(houseId, "M-01", null, null, "Motivo", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Fine(houseId, "M-01", fineType, null, "   ", null, null));
        assertThrows(IllegalArgumentException.class, () -> new Fine(houseId, "M-01", fineType, new BigDecimal("-10.00"), "Motivo", null, null));

        Fine validFine = new Fine(houseId, "M-01", fineType, null, "Motivo", null, null);
        assertThrows(IllegalArgumentException.class, () -> validFine.markAsPaid(null));
    }

    @Test
    @DisplayName("FineType Invariant: Validation on construction")
    void testFineTypeValidation() {
        FineType type = new FineType("noise", " Ruidos ", "Exceso", new BigDecimal("300.00"));
        assertEquals("NOISE", type.getCode());
        assertEquals("Ruidos", type.getName());
        assertTrue(type.isActive());

        assertThrows(IllegalArgumentException.class, () -> new FineType(null, "Name", null, new BigDecimal("100.00")));
        assertThrows(IllegalArgumentException.class, () -> new FineType("   ", "Name", null, new BigDecimal("100.00")));
        assertThrows(IllegalArgumentException.class, () -> new FineType("CODE", null, null, new BigDecimal("100.00")));
        assertThrows(IllegalArgumentException.class, () -> new FineType("CODE", "  ", null, new BigDecimal("100.00")));
        assertThrows(IllegalArgumentException.class, () -> new FineType("CODE", "Name", null, null));
        assertThrows(IllegalArgumentException.class, () -> new FineType("CODE", "Name", null, BigDecimal.ZERO));
        assertThrows(IllegalArgumentException.class, () -> new FineType("CODE", "Name", null, new BigDecimal("-50.00")));
    }
}
