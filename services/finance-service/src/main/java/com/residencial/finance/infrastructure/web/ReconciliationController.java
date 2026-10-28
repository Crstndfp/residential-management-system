package com.residencial.finance.infrastructure.web;

import com.residencial.finance.application.dto.ReconciliationSummaryResponse;
import com.residencial.finance.application.usecase.ReconcileBankPaymentsUseCase;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/finance/reconciliation")
public class ReconciliationController {

    private final ReconcileBankPaymentsUseCase reconcileBankPaymentsUseCase;

    public ReconciliationController(ReconcileBankPaymentsUseCase reconcileBankPaymentsUseCase) {
        this.reconcileBankPaymentsUseCase = reconcileBankPaymentsUseCase;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReconciliationSummaryResponse> uploadBankReport(
            @RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("The uploaded bank statement file is empty.");
        }
        ReconciliationSummaryResponse response = reconcileBankPaymentsUseCase.execute(file);
        return ResponseEntity.ok(response);
    }
}
