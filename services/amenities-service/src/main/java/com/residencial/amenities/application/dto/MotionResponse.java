package com.residencial.amenities.application.dto;

import com.residencial.amenities.domain.model.AssemblyMotion;

import java.time.Instant;
import java.util.UUID;

public record MotionResponse(
        UUID id,
        UUID assemblyId,
        String title,
        String description,
        String status,
        String verdict,
        Instant openedAt,
        Instant closedAt,
        Instant createdAt
) {
    public static MotionResponse fromDomain(AssemblyMotion motion) {
        return new MotionResponse(
                motion.getId(),
                motion.getAssemblyId(),
                motion.getTitle(),
                motion.getDescription(),
                motion.getStatus().name(),
                motion.getVerdict() != null ? motion.getVerdict().name() : null,
                motion.getOpenedAt(),
                motion.getClosedAt(),
                motion.getCreatedAt()
        );
    }
}
