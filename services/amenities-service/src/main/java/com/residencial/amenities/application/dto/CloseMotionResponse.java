package com.residencial.amenities.application.dto;

import java.time.Instant;
import java.util.UUID;

public record CloseMotionResponse(
        UUID motionId,
        String title,
        String status,
        String verdict,
        long inFavorCount,
        long againstCount,
        long abstainCount,
        long totalVotes,
        double approvalPercentage,
        Instant closedAt
) {
}
