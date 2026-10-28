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
}
