package com.residencial.amenities.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GovernanceDomainTest {

    @Test
    @DisplayName("Assembly lifecycle transitions correctly from CONVOKED to IN_PROGRESS to CONCLUDED")
    void assemblyLifecycleSuccess() {
        Assembly assembly = new Assembly(
                UUID.randomUUID(),
                "Annual General Meeting 2026",
                AssemblyType.ORDINARY,
                Instant.now().plusSeconds(3600),
                "1. Budget approval\n2. Board election",
                AssemblyStatus.CONVOKED,
                100,
                Instant.now(),
                Instant.now()
        );

        assertThat(assembly.getStatus()).isEqualTo(AssemblyStatus.CONVOKED);

        assembly.startAssembly();
        assertThat(assembly.getStatus()).isEqualTo(AssemblyStatus.IN_PROGRESS);

        assembly.concludeAssembly();
        assertThat(assembly.getStatus()).isEqualTo(AssemblyStatus.CONCLUDED);
    }

    @Test
    @DisplayName("Starting an already concluded assembly throws IllegalStateException")
    void cannotStartConcludedAssembly() {
        Assembly assembly = new Assembly(
                UUID.randomUUID(),
                "Extraordinary Meeting",
                AssemblyType.EXTRAORDINARY,
                Instant.now().plusSeconds(3600),
                "Security repairs",
                AssemblyStatus.CONCLUDED,
                80,
                Instant.now(),
                Instant.now()
        );

        assertThatThrownBy(assembly::startAssembly)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot start an assembly that is CONCLUDED");
    }

    @Test
    @DisplayName("AssemblyMotion lifecycle transitions from PENDING to OPEN to CLOSED")
    void motionLifecycleSuccess() {
        AssemblyMotion motion = new AssemblyMotion(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Roof Reparation Budget",
                "Approve $5,000 for clubhouse roof maintenance",
                MotionStatus.PENDING,
                null,
                null,
                null,
                Instant.now()
        );

        assertThat(motion.getStatus()).isEqualTo(MotionStatus.PENDING);
        assertThat(motion.getVerdict()).isNull();

        motion.open();
        assertThat(motion.getStatus()).isEqualTo(MotionStatus.OPEN);
        assertThat(motion.getOpenedAt()).isNotNull();

        motion.close(MotionVerdict.APPROVED);
        assertThat(motion.getStatus()).isEqualTo(MotionStatus.CLOSED);
        assertThat(motion.getVerdict()).isEqualTo(MotionVerdict.APPROVED);
        assertThat(motion.getClosedAt()).isNotNull();
    }

    @Test
    @DisplayName("Reopening a closed motion throws IllegalStateException")
    void cannotReopenClosedMotion() {
        AssemblyMotion motion = new AssemblyMotion(
                UUID.randomUUID(),
                UUID.randomUUID(),
                "Motion 1",
                "Description",
                MotionStatus.CLOSED,
                MotionVerdict.REJECTED,
                Instant.now().minusSeconds(100),
                Instant.now(),
                Instant.now().minusSeconds(200)
        );

        assertThatThrownBy(motion::open)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot reopen a closed motion");
    }

    @Test
    @DisplayName("MotionVote computes a non-null, 64-char SHA-256 hash")
    void motionVoteHashCalculation() {
        UUID motionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        Instant now = Instant.now();

        MotionVote vote = new MotionVote(
                UUID.randomUUID(),
                motionId,
                houseId,
                "B-102",
                userId,
                VoteChoice.IN_FAVOR,
                null,
                now
        );

        assertThat(vote.getVoteHash()).isNotNull();
        assertThat(vote.getVoteHash()).hasSize(64);

        // Verification of deterministic calculation
        String expectedHash = MotionVote.calculateHash(motionId, houseId, VoteChoice.IN_FAVOR, now);
        assertThat(vote.getVoteHash()).isEqualTo(expectedHash);
    }
}
