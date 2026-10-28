package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.MotionVote;
import com.residencial.amenities.domain.model.VoteChoice;
import com.residencial.amenities.domain.repository.MotionVoteRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MotionVoteRepositoryAdapter implements MotionVoteRepository {

    private final JpaMotionVoteRepository jpaVoteRepository;

    public MotionVoteRepositoryAdapter(JpaMotionVoteRepository jpaVoteRepository) {
        this.jpaVoteRepository = jpaVoteRepository;
    }

    @Override
    public MotionVote save(MotionVote vote) {
        return jpaVoteRepository.save(vote);
    }

    @Override
    public boolean existsByMotionIdAndHouseId(UUID motionId, UUID houseId) {
        return jpaVoteRepository.existsByMotionIdAndHouseId(motionId, houseId);
    }

    @Override
    public List<MotionVote> findByMotionId(UUID motionId) {
        return jpaVoteRepository.findByMotionId(motionId);
    }

    @Override
    public long countByMotionId(UUID motionId) {
        return jpaVoteRepository.countByMotionId(motionId);
    }

    @Override
    public long countByMotionIdAndVoteChoice(UUID motionId, VoteChoice voteChoice) {
        return jpaVoteRepository.countByMotionIdAndVoteChoice(motionId, voteChoice);
    }
}
