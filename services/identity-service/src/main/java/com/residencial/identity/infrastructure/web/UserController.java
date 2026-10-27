package com.residencial.identity.infrastructure.web;

import com.residencial.identity.application.dto.ActivateAccountRequest;
import com.residencial.identity.application.dto.ActivateAccountResponse;
import com.residencial.identity.application.usecase.ActivateAccountUseCase;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/identity/users")
public class UserController {

    private final ActivateAccountUseCase activateAccountUseCase;

    public UserController(ActivateAccountUseCase activateAccountUseCase) {
        this.activateAccountUseCase = activateAccountUseCase;
    }

    @PostMapping("/activate")
    public ResponseEntity<ActivateAccountResponse> activateAccount(@Valid @RequestBody ActivateAccountRequest request) {
        ActivateAccountResponse response = activateAccountUseCase.execute(request);
        return ResponseEntity.ok(response);
    }
}
