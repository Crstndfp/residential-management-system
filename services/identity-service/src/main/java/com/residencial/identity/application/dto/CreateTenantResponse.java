package com.residencial.identity.application.dto;

import com.residencial.identity.domain.model.ResidentialRole;
import java.time.Instant;
import java.util.UUID;

public record CreateTenantResponse(
        UUID tenantId,
        UUID houseId,
        String houseNumber,
        String email,
        String fullName,
        ResidentialRole residentialRole,
        String invitationToken,
        Instant tokenExpiresAt,
        Instant createdAt
) {}
