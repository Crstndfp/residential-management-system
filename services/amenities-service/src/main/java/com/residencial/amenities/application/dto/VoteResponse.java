package com.residencial.amenities.application.dto;

import com.residencial.amenities.domain.model.MotionVote;

import java.time.Instant;
import java.util.UUID;

public record VoteResponse(
        UUID id,
        UUID motionId,
        UUID houseId,
        String houseNumber,
        UUID voterUserId,
        String voteChoice,
        String voteHash,
        Instant createdAt
) {
    public static VoteResponse fromDomain(MotionVote vote) {
        return new VoteResponse(
                vote.getId(),
                vote.getMotionId(),
                vote.getHouseId(),
                vote.getHouseNumber(),
                vote.getVoterUserId(),
                vote.getVoteChoice().name(),
                vote.getVoteHash(),
                vote.getCreatedAt()
        );
    }
}
