package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.CloseMotionResponse;
import com.residencial.amenities.domain.exception.MotionNotFoundException;
import com.residencial.amenities.domain.model.AssemblyMotion;
import com.residencial.amenities.domain.model.MotionStatus;
import com.residencial.amenities.domain.model.VoteChoice;
import com.residencial.amenities.domain.repository.AssemblyMotionRepository;
import com.residencial.amenities.domain.repository.MotionVoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CloseMotionUseCaseTest {

    private AssemblyMotionRepository motionRepository;
    private MotionVoteRepository voteRepository;
    private CloseMotionUseCase closeMotionUseCase;

    @BeforeEach
    void setUp() {
        motionRepository = mock(AssemblyMotionRepository.class);
        voteRepository = mock(MotionVoteRepository.class);
        closeMotionUseCase = new CloseMotionUseCase(motionRepository, voteRepository);
    }

    @Test
    @DisplayName("Closing motion computes tally and sets verdict to APPROVED when inFavor > against")
    void closeMotionApproved() {
        UUID motionId = UUID.randomUUID();
        AssemblyMotion motion = new AssemblyMotion(
                motionId,
                UUID.randomUUID(),
                "Solar Panel Installation",
                "Install solar panels on clubhouse",
                MotionStatus.OPEN,
                null,
                Instant.now().minusSeconds(3600),
                null,
                Instant.now().minusSeconds(7200)
        );

        when(motionRepository.findById(motionId)).thenReturn(Optional.of(motion));
        when(voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.IN_FAVOR)).thenReturn(30L);
        when(voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.AGAINST)).thenReturn(10L);
        when(voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.ABSTAIN)).thenReturn(5L);
        when(motionRepository.save(any(AssemblyMotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CloseMotionResponse response = closeMotionUseCase.execute(motionId);

        assertThat(response).isNotNull();
        assertThat(response.motionId()).isEqualTo(motionId);
        assertThat(response.status()).isEqualTo("CLOSED");
        assertThat(response.verdict()).isEqualTo("APPROVED");
        assertThat(response.inFavorCount()).isEqualTo(30L);
        assertThat(response.againstCount()).isEqualTo(10L);
        assertThat(response.abstainCount()).isEqualTo(5L);
        assertThat(response.totalVotes()).isEqualTo(45L);
        // 30 / 45 * 100 = 66.67%
        assertThat(response.approvalPercentage()).isEqualTo(66.67);
        assertThat(response.closedAt()).isNotNull();
    }

    @Test
    @DisplayName("Closing motion sets verdict to REJECTED when against > inFavor")
    void closeMotionRejected() {
        UUID motionId = UUID.randomUUID();
        AssemblyMotion motion = new AssemblyMotion(
                motionId,
                UUID.randomUUID(),
                "Increase Admin Fee",
                "Increase fee by 25%",
                MotionStatus.OPEN,
                null,
                Instant.now().minusSeconds(1800),
                null,
                Instant.now().minusSeconds(3600)
        );

        when(motionRepository.findById(motionId)).thenReturn(Optional.of(motion));
        when(voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.IN_FAVOR)).thenReturn(12L);
        when(voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.AGAINST)).thenReturn(28L);
        when(voteRepository.countByMotionIdAndVoteChoice(motionId, VoteChoice.ABSTAIN)).thenReturn(2L);
        when(motionRepository.save(any(AssemblyMotion.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CloseMotionResponse response = closeMotionUseCase.execute(motionId);

        assertThat(response.verdict()).isEqualTo("REJECTED");
        assertThat(response.approvalPercentage()).isEqualTo(28.57); // 12/42 * 100
    }

    @Test
    @DisplayName("Closing non-existent motion throws MotionNotFoundException")
    void closeMotionNotFound() {
        UUID motionId = UUID.randomUUID();
        when(motionRepository.findById(motionId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> closeMotionUseCase.execute(motionId))
                .isInstanceOf(MotionNotFoundException.class);
    }
}
