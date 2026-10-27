package com.residencial.identity.application.usecase;

import com.residencial.identity.application.dto.ActivateAccountRequest;
import com.residencial.identity.application.dto.ActivateAccountResponse;
import com.residencial.identity.domain.exception.InvalidOrExpiredTokenException;
import com.residencial.identity.domain.model.InvitationToken;
import com.residencial.identity.domain.model.User;
import com.residencial.identity.domain.repository.InvitationTokenRepository;
import com.residencial.identity.domain.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Transactional
public class ActivateAccountUseCase {

    private final InvitationTokenRepository invitationTokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ActivateAccountUseCase(
            InvitationTokenRepository invitationTokenRepository,
            UserRepository userRepository) {
        this.invitationTokenRepository = invitationTokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public ActivateAccountResponse execute(ActivateAccountRequest request) {
        InvitationToken token = invitationTokenRepository.findByToken(request.token().trim())
                .filter(InvitationToken::isValid)
                .orElseThrow(InvalidOrExpiredTokenException::new);

        User user = userRepository.findById(token.getUserId())
                .orElseThrow(InvalidOrExpiredTokenException::new);

        String hashedPassword = passwordEncoder.encode(request.password());
        user.activateAccount(hashedPassword);
        token.markAsUsed();

        userRepository.save(user);
        invitationTokenRepository.save(token);

        return new ActivateAccountResponse(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getResidentialRole(),
                user.isActive(),
                Instant.now()
        );
    }
}
