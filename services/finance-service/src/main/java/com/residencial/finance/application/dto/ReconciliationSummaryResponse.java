package com.residencial.finance.application.dto;

import java.math.BigDecimal;
import java.util.List;

public record ReconciliationSummaryResponse(
        String fileName,
        int totalRowsRead,
        int successfullyReconciled,
        int duplicateTransactions,
        int unmatchedTransactions,
        BigDecimal totalAmountProcessed,
        List<String> auditMessages
) {}
