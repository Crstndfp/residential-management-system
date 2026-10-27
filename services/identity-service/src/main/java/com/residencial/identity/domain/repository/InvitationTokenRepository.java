package com.residencial.identity.domain.repository;

import com.residencial.identity.domain.model.InvitationToken;
import java.util.Optional;
import java.util.UUID;

public interface InvitationTokenRepository {
    InvitationToken save(InvitationToken token);
    Optional<InvitationToken> findByToken(String token);
    Optional<InvitationToken> findByUserIdAndIsUsedFalse(UUID userId);
}
