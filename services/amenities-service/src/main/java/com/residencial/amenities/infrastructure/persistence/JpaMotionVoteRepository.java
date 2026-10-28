package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.MotionVote;
import com.residencial.amenities.domain.model.VoteChoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaMotionVoteRepository extends JpaRepository<MotionVote, UUID> {
    boolean existsByMotionIdAndHouseId(UUID motionId, UUID houseId);
    List<MotionVote> findByMotionId(UUID motionId);
    long countByMotionId(UUID motionId);
    long countByMotionIdAndVoteChoice(UUID motionId, VoteChoice voteChoice);
}
