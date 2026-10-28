package com.residencial.finance.infrastructure.web;

import com.residencial.finance.application.dto.RegisterInvoiceRequest;
import com.residencial.finance.application.dto.RegisterInvoiceResponse;
import com.residencial.finance.application.usecase.RegisterExpenseInvoiceUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finance/invoices")
public class InvoiceController {

    private final RegisterExpenseInvoiceUseCase registerExpenseInvoiceUseCase;

    public InvoiceController(RegisterExpenseInvoiceUseCase registerExpenseInvoiceUseCase) {
        this.registerExpenseInvoiceUseCase = registerExpenseInvoiceUseCase;
    }

    @PostMapping
    public ResponseEntity<RegisterInvoiceResponse> registerInvoice(@Valid @RequestBody RegisterInvoiceRequest request) {
        RegisterInvoiceResponse response = registerExpenseInvoiceUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
