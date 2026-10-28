package com.residencial.finance.application.dto;

import com.residencial.finance.domain.model.InvoiceStatus;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record RegisterInvoiceResponse(
        UUID invoiceId,
        String invoiceNumber,
        UUID vendorId,
        String vendorName,
        String vendorTaxId,
        String categoryCode,
        String categoryName,
        LocalDate issueDate,
        LocalDate dueDate,
        BigDecimal amount,
        InvoiceStatus status,
        String documentUrl,
        Instant createdAt
) {}
