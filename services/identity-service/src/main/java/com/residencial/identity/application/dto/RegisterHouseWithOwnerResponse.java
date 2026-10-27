package com.residencial.identity.application.dto;

import java.time.Instant;
import java.util.UUID;

public record RegisterHouseWithOwnerResponse(
        UUID houseId,
        String houseNumber,
        String streetOrBlock,
        UUID ownerId,
        String ownerFullName,
        String ownerEmail,
        String invitationToken,
        Instant tokenExpiresAt,
        Instant createdAt
) {}
