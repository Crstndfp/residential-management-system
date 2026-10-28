package com.residencial.finance.infrastructure.web;

import com.residencial.finance.application.dto.AccountStatementResponse;
import com.residencial.finance.application.usecase.GetAccountStatementUseCase;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance/account-statements")
public class AccountStatementController {

    private final GetAccountStatementUseCase getAccountStatementUseCase;

    public AccountStatementController(GetAccountStatementUseCase getAccountStatementUseCase) {
        this.getAccountStatementUseCase = getAccountStatementUseCase;
    }

    @GetMapping("/{houseId}")
    public ResponseEntity<AccountStatementResponse> getAccountStatement(
            @PathVariable UUID houseId,
            @RequestHeader(value = "X-User-Role", required = false, defaultValue = "OWNER") String userRole) {
        AccountStatementResponse response = getAccountStatementUseCase.execute(houseId, userRole);
        return ResponseEntity.ok(response);
    }
}
