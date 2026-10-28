package com.residencial.finance.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedBankRecord(
        String authorizationNumber,
        String houseReference,
        LocalDate transactionDate,
        BigDecimal amount
) {}
