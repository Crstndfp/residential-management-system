package com.residencial.identity.infrastructure.persistence;

import com.residencial.identity.domain.model.InvitationToken;
import com.residencial.identity.domain.repository.InvitationTokenRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class InvitationTokenRepositoryAdapter implements InvitationTokenRepository {

    private final JpaInvitationTokenRepository jpaInvitationTokenRepository;

    public InvitationTokenRepositoryAdapter(JpaInvitationTokenRepository jpaInvitationTokenRepository) {
        this.jpaInvitationTokenRepository = jpaInvitationTokenRepository;
    }

    @Override
    public InvitationToken save(InvitationToken token) {
        return jpaInvitationTokenRepository.save(token);
    }

    @Override
    public Optional<InvitationToken> findByToken(String token) {
        return jpaInvitationTokenRepository.findByToken(token);
    }

    @Override
    public Optional<InvitationToken> findByUserIdAndIsUsedFalse(UUID userId) {
        return jpaInvitationTokenRepository.findByUserIdAndIsUsedFalse(userId);
    }
}
