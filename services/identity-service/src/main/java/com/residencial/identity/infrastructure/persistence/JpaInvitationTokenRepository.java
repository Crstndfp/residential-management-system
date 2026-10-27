package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.InvitationToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaInvitationTokenRepository extends JpaRepository<InvitationToken, UUID> {
    Optional<InvitationToken> findByToken(String token);
    Optional<InvitationToken> findByUserIdAndIsUsedFalse(UUID userId);
}
