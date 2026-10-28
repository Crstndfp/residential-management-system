package com.residencial.finance.application.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record RegisterInvoiceRequest(
        @NotBlank(message = "Invoice number is required")
        @Size(max = 50, message = "Invoice number cannot exceed 50 characters")
        String invoiceNumber,

        @NotBlank(message = "Vendor Tax ID (NIT/RFC) is required")
        @Size(max = 30, message = "Vendor Tax ID cannot exceed 30 characters")
        String vendorTaxId,

        @NotBlank(message = "Vendor name is required")
        @Size(max = 150, message = "Vendor name cannot exceed 150 characters")
        String vendorName,

        @Size(max = 100, message = "Contact email cannot exceed 100 characters")
        String vendorEmail,

        @Size(max = 30, message = "Contact phone cannot exceed 30 characters")
        String vendorPhone,

        @NotBlank(message = "Expense category code is required")
        @Size(max = 50, message = "Category code cannot exceed 50 characters")
        String categoryCode,

        @NotNull(message = "Issue date is required")
        LocalDate issueDate,

        @NotNull(message = "Due date is required")
        LocalDate dueDate,

        @NotNull(message = "Invoice amount is required")
        @DecimalMin(value = "0.01", message = "Invoice amount must be greater than zero")
        BigDecimal amount,

        @Size(max = 255, message = "Document URL cannot exceed 255 characters")
        String documentUrl,

        String notes
) {}
