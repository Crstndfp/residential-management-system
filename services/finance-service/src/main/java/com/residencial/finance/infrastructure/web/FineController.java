package com.residencial.finance.infrastructure.web;

import com.residencial.finance.application.dto.ApplyFineRequest;
import com.residencial.finance.application.dto.ApplyFineResponse;
import com.residencial.finance.application.usecase.ApplyFineToHouseUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/finance/fines")
public class FineController {

    private final ApplyFineToHouseUseCase applyFineToHouseUseCase;

    public FineController(ApplyFineToHouseUseCase applyFineToHouseUseCase) {
        this.applyFineToHouseUseCase = applyFineToHouseUseCase;
    }

    @PostMapping
    public ResponseEntity<ApplyFineResponse> applyFine(@Valid @RequestBody ApplyFineRequest request) {
        ApplyFineResponse response = applyFineToHouseUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
