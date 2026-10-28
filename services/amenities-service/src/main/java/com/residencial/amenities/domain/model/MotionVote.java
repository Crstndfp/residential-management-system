package com.residencial.amenities.domain.model;

import jakarta.persistence.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

@Entity
@Table(name = "motion_votes", schema = "amenities_schema")
public class MotionVote {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "motion_id", nullable = false)
    private UUID motionId;

    @Column(name = "house_id", nullable = false)
    private UUID houseId;

    @Column(name = "house_number", nullable = false, length = 20)
    private String houseNumber;

    @Column(name = "voter_user_id", nullable = false)
    private UUID voterUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "vote_choice", nullable = false, length = 20)
    private VoteChoice voteChoice;

    @Column(name = "vote_hash", nullable = false, length = 64)
    private String voteHash;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected MotionVote() {
    }

    public MotionVote(UUID id, UUID motionId, UUID houseId, String houseNumber,
                      UUID voterUserId, VoteChoice voteChoice, String voteHash, Instant createdAt) {
        if (motionId == null) {
            throw new IllegalArgumentException("Motion ID cannot be null");
        }
        if (houseId == null) {
            throw new IllegalArgumentException("House ID cannot be null");
        }
        if (houseNumber == null || houseNumber.isBlank()) {
            throw new IllegalArgumentException("House number cannot be blank");
        }
        if (voterUserId == null) {
            throw new IllegalArgumentException("Voter user ID cannot be null");
        }
        if (voteChoice == null) {
            throw new IllegalArgumentException("Vote choice cannot be null");
        }

        this.id = id != null ? id : UUID.randomUUID();
        this.motionId = motionId;
        this.houseId = houseId;
        this.houseNumber = houseNumber;
        this.voterUserId = voterUserId;
        this.voteChoice = voteChoice;
        this.createdAt = createdAt != null ? createdAt : Instant.now();
        this.voteHash = (voteHash != null && !voteHash.isBlank())
                ? voteHash
                : calculateHash(this.motionId, this.houseId, this.voteChoice, this.createdAt);
    }

    public static String calculateHash(UUID motionId, UUID houseId, VoteChoice choice, Instant timestamp) {
        try {
            String raw = motionId.toString() + ":" + houseId.toString() + ":" + choice.name() + ":" + timestamp.toString();
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getMotionId() {
        return motionId;
    }

    public UUID getHouseId() {
        return houseId;
    }

    public String getHouseNumber() {
        return houseNumber;
    }

    public UUID getVoterUserId() {
        return voterUserId;
    }

    public VoteChoice getVoteChoice() {
        return voteChoice;
    }

    public String getVoteHash() {
        return voteHash;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
