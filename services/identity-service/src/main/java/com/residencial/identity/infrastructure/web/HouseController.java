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

    public HouseController(RegisterHouseWithOwnerUseCase registerHouseWithOwnerUseCase) {
        this.registerHouseWithOwnerUseCase = registerHouseWithOwnerUseCase;
    }

    @PostMapping("/register-owner")
    public ResponseEntity<RegisterHouseWithOwnerResponse> registerHouseWithOwner(
            @Valid @RequestBody RegisterHouseWithOwnerRequest request) {
        RegisterHouseWithOwnerResponse response = registerHouseWithOwnerUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
