package com.residencial.identity.application.dto;

import com.residencial.identity.domain.model.ResidentialRole;
import java.time.Instant;
import java.util.UUID;

public record ActivateAccountResponse(
        UUID userId,
        String email,
        String fullName,
        ResidentialRole residentialRole,
        boolean isActive,
        Instant activatedAt
) {}
