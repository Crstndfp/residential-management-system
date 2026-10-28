package com.residencial.amenities.domain.repository;

import com.residencial.amenities.domain.model.MotionVote;
import com.residencial.amenities.domain.model.VoteChoice;

import java.util.List;
import java.util.UUID;

public interface MotionVoteRepository {
    MotionVote save(MotionVote vote);
    boolean existsByMotionIdAndHouseId(UUID motionId, UUID houseId);
    List<MotionVote> findByMotionId(UUID motionId);
    long countByMotionId(UUID motionId);
    long countByMotionIdAndVoteChoice(UUID motionId, VoteChoice voteChoice);
}
