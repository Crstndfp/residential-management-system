package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.CloseMotionResponse;
import com.residencial.amenities.domain.exception.MotionNotFoundException;
import com.residencial.amenities.domain.model.AssemblyMotion;
import com.residencial.amenities.domain.model.MotionVerdict;
import com.residencial.amenities.domain.model.VoteChoice;
import com.residencial.amenities.domain.repository.AssemblyMotionRepository;
import com.residencial.amenities.domain.repository.MotionVoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

@Service
public class CloseMotionUseCase {

    private final AssemblyMotionRepository motionRepository;
    private final MotionVoteRepository voteRepository;

    public CloseMotionUseCase(AssemblyMotionRepository motionRepository,
                              MotionVoteRepository voteRepository) {
        this.motionRepository = motionRepository;
        this.voteRepository = voteRepository;
    }

    @Transactional
    public CloseMotionResponse execute(UUID motionId) {
        AssemblyMotion motion = motionRepository.findById(motionId)
                .orElseThrow(() -> new MotionNotFoundException(motionId));

        long inFavor = voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.IN_FAVOR);
        long against = voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.AGAINST);
        long abstain = voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.ABSTAIN);
        long totalVotes = inFavor + against + abstain;

        MotionVerdict verdict;
        if (inFavor > against) {
            verdict = MotionVerdict.APPROVED;
        } else if (against > inFavor) {
            verdict = MotionVerdict.REJECTED;
        } else if (totalVotes > 0) {
            verdict = MotionVerdict.TIE;
        } else {
            verdict = MotionVerdict.REJECTED;
        }

        double approvalPercentage = 0.0;
        if (totalVotes > 0) {
            double rawPercentage = (inFavor * 100.0) / totalVotes;
            approvalPercentage = BigDecimal.valueOf(rawPercentage)
                    .setScale(2, RoundingMode.HALF_UP)
                    .doubleValue();
        }

        motion.close(verdict);
        AssemblyMotion saved = motionRepository.save(motion);

        return new CloseMotionResponse(
                saved.getId(),
                saved.getTitle(),
                saved.getStatus().name(),
                saved.getVerdict().name(),
                inFavor,
                against,
                abstain,
                totalVotes,
                approvalPercentage,
                saved.getClosedAt()
        );
    }
}
