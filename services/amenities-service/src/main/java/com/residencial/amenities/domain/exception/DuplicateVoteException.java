package com.residencial.amenities.domain.exception;

import java.util.UUID;

public class DuplicateVoteException extends AmenitiesDomainException {
    public DuplicateVoteException(UUID motionId, UUID houseId) {
        super("DUPLICATE_VOTE_CAST", "House " + houseId + " has already cast a vote for motion " + motionId);
    }
}
