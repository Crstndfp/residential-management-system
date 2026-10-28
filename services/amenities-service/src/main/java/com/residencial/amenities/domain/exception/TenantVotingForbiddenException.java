package com.residencial.amenities.domain.exception;

public class TenantVotingForbiddenException extends AmenitiesDomainException {
    public TenantVotingForbiddenException(String role) {
        super("TENANT_VOTING_FORBIDDEN", "Only property owners can vote in assembly motions. Current role: " + role);
    }
}
