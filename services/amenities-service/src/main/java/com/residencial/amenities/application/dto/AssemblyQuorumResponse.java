package com.residencial.amenities.application.dto;

import java.util.UUID;

public record AssemblyQuorumResponse(
        UUID assemblyId,
        String assemblyTitle,
        String assemblyStatus,
        int totalResidentialHouses,
        long presentHouses,
        double quorumPercentage,
        boolean hasQuorum
) {
}
