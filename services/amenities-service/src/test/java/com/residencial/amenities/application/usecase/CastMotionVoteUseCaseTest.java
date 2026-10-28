package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.CastVoteRequest;
import com.residencial.amenities.application.dto.VoteResponse;
import com.residencial.amenities.domain.exception.DuplicateVoteException;
import com.residencial.amenities.domain.exception.MotionNotFoundException;
import com.residencial.amenities.domain.exception.MotionNotOpenException;
import com.residencial.amenities.domain.exception.TenantVotingForbiddenException;
import com.residencial.amenities.domain.model.AssemblyMotion;
import com.residencial.amenities.domain.model.MotionStatus;
import com.residencial.amenities.domain.model.MotionVote;
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

class CastMotionVoteUseCaseTest {

    private AssemblyMotionRepository motionRepository;
    private MotionVoteRepository voteRepository;
    private CastMotionVoteUseCase castMotionVoteUseCase;

    @BeforeEach
    void setUp() {
        motionRepository = mock(AssemblyMotionRepository.class);
        voteRepository = mock(MotionVoteRepository.class);
        castMotionVoteUseCase = new CastMotionVoteUseCase(motionRepository, voteRepository);
    }

    @Test
    @DisplayName("Successfully casts vote when user has OWNER role and motion is OPEN")
    void castVoteSuccess() {
        UUID motionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        AssemblyMotion motion = new AssemblyMotion(
                motionId,
                UUID.randomUUID(),
                "Install CCTV",
                "Approve budget",
                MotionStatus.OPEN,
                null,
                Instant.now(),
                null,
                Instant.now()
        );

        when(motionRepository.findById(motionId)).thenReturn(Optional.of(motion));
        when(voteRepository.existsByMotionIdAndHouseId(motionId, houseId)).thenReturn(false);
        when(voteRepository.save(any(MotionVote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CastVoteRequest request = new CastVoteRequest(
                houseId,
                "B-204",
                userId,
                "IN_FAVOR"
        );

        VoteResponse response = castMotionVoteUseCase.execute(motionId, request, "OWNER");

        assertThat(response).isNotNull();
        assertThat(response.motionId()).isEqualTo(motionId);
        assertThat(response.houseId()).isEqualTo(houseId);
        assertThat(response.voteChoice()).isEqualTo("IN_FAVOR");
        assertThat(response.voteHash()).isNotNull();
        assertThat(response.voteHash()).hasSize(64);
    }

    @Test
    @DisplayName("Throws TenantVotingForbiddenException when user role is TENANT")
    void tenantCannotVote() {
        UUID motionId = UUID.randomUUID();
        CastVoteRequest request = new CastVoteRequest(
                UUID.randomUUID(),
                "B-204",
                UUID.randomUUID(),
                "IN_FAVOR"
        );

        assertThatThrownBy(() -> castMotionVoteUseCase.execute(motionId, request, "TENANT"))
                .isInstanceOf(TenantVotingForbiddenException.class)
                .hasMessageContaining("Only property owners can vote");
    }

    @Test
    @DisplayName("Throws MotionNotOpenException when motion status is PENDING or CLOSED")
    void motionNotOpenThrowsException() {
        UUID motionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();

        AssemblyMotion motion = new AssemblyMotion(
                motionId,
                UUID.randomUUID(),
                "Install CCTV",
                "Approve budget",
                MotionStatus.PENDING,
                null,
                null,
                null,
                Instant.now()
        );

        when(motionRepository.findById(motionId)).thenReturn(Optional.of(motion));

        CastVoteRequest request = new CastVoteRequest(
                houseId,
                "B-204",
                UUID.randomUUID(),
                "IN_FAVOR"
        );

        assertThatThrownBy(() -> castMotionVoteUseCase.execute(motionId, request, "OWNER"))
                .isInstanceOf(MotionNotOpenException.class)
                .hasMessageContaining("is not open for voting");
    }

    @Test
    @DisplayName("Throws DuplicateVoteException when house has already cast a vote for this motion")
    void duplicateVoteThrowsException() {
        UUID motionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();

        AssemblyMotion motion = new AssemblyMotion(
                motionId,
                UUID.randomUUID(),
                "Install CCTV",
                "Approve budget",
                MotionStatus.OPEN,
                null,
                Instant.now(),
                null,
                Instant.now()
        );

        when(motionRepository.findById(motionId)).thenReturn(Optional.of(motion));
        when(voteRepository.existsByMotionIdAndHouseId(motionId, houseId)).thenReturn(true);

        CastVoteRequest request = new CastVoteRequest(
                houseId,
                "B-204",
                UUID.randomUUID(),
                "AGAINST"
        );

        assertThatThrownBy(() -> castMotionVoteUseCase.execute(motionId, request, "OWNER"))
                .isInstanceOf(DuplicateVoteException.class)
                .hasMessageContaining("has already cast a vote for motion");
    }
}
