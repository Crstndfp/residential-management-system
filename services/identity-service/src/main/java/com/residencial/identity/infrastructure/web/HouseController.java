package com.residencial.identity.infrastructure.web;

import com.residencial.identity.application.dto.RegisterHouseWithOwnerRequest;
import com.residencial.identity.application.dto.RegisterHouseWithOwnerResponse;
import com.residencial.identity.application.usecase.RegisterHouseWithOwnerUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/houses")
public class HouseController {

    private final RegisterHouseWithOwnerUseCase registerHouseWithOwnerUseCase;
    private final com.residencial.identity.application.usecase.CreateTenantUseCase createTenantUseCase;

    public HouseController(
            RegisterHouseWithOwnerUseCase registerHouseWithOwnerUseCase,
            com.residencial.identity.application.usecase.CreateTenantUseCase createTenantUseCase) {
        this.registerHouseWithOwnerUseCase = registerHouseWithOwnerUseCase;
        this.createTenantUseCase = createTenantUseCase;
    }

    @PostMapping("/register-owner")
    public ResponseEntity<RegisterHouseWithOwnerResponse> registerHouseWithOwner(
            @Valid @RequestBody RegisterHouseWithOwnerRequest request) {
        RegisterHouseWithOwnerResponse response = registerHouseWithOwnerUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{houseId}/tenants")
    public ResponseEntity<com.residencial.identity.application.dto.CreateTenantResponse> createTenant(
            @org.springframework.web.bind.annotation.PathVariable java.util.UUID houseId,
            @Valid @RequestBody com.residencial.identity.application.dto.CreateTenantRequest request) {
        com.residencial.identity.application.dto.CreateTenantResponse response = createTenantUseCase.execute(houseId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
