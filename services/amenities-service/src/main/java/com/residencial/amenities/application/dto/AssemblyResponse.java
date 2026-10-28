package com.residencial.amenities.application.dto;

import com.residencial.amenities.domain.model.Assembly;

import java.time.Instant;
import java.util.UUID;

public record AssemblyResponse(
        UUID id,
        String title,
        String assemblyType,
        Instant scheduledAt,
        String agenda,
        String status,
        int totalResidentialHouses,
        Instant createdAt,
        Instant updatedAt
) {
    public static AssemblyResponse fromDomain(Assembly assembly) {
        return new AssemblyResponse(
                assembly.getId(),
                assembly.getTitle(),
                assembly.getAssemblyType().name(),
                assembly.getScheduledAt(),
                assembly.getAgenda(),
                assembly.getStatus().name(),
                assembly.getTotalResidentialHouses(),
                assembly.getCreatedAt(),
                assembly.getUpdatedAt()
        );
    }
}
