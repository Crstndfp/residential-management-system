package com.residencial.identity.application.usecase;

import com.residencial.identity.application.dto.ActivateAccountRequest;
import com.residencial.identity.application.dto.ActivateAccountResponse;
import com.residencial.identity.domain.exception.InvalidOrExpiredTokenException;
import com.residencial.identity.domain.model.*;
import com.residencial.identity.domain.repository.InvitationTokenRepository;
import com.residencial.identity.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ActivateAccountUseCaseTest {

    @Mock
    private InvitationTokenRepository invitationTokenRepository;

    @Mock
    private UserRepository userRepository;

    private ActivateAccountUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ActivateAccountUseCase(invitationTokenRepository, userRepository);
    }

    @Test
    @DisplayName("US-102: Successfully activate user account with valid token and strong password")
    void shouldActivateAccountSuccessfully() {
        UUID userId = UUID.randomUUID();
        String rawToken = "valid-secret-token-xyz";
        Instant futureExpiry = Instant.now().plus(24, ChronoUnit.HOURS);
        InvitationToken token = new InvitationToken(userId, rawToken, futureExpiry);

        House house = new House("H-201", "Boulevard");
        IdentificationDocument doc = new IdentificationDocument(IdentificationType.PASSPORT, "P998877");
        User user = new User(house, doc, "owner@example.com", "Carlos Owner", ResidentialRole.OWNER);

        when(invitationTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(token));
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ActivateAccountRequest request = new ActivateAccountRequest(rawToken, "SecretPassword123!");
        ActivateAccountResponse response = useCase.execute(request);

        assertNotNull(response);
        assertTrue(response.isActive());
        assertEquals("owner@example.com", response.email());
        assertTrue(token.isUsed());
        assertNotNull(user.getPasswordHash());

        verify(userRepository).save(user);
        verify(invitationTokenRepository).save(token);
    }

    @Test
    @DisplayName("US-102: Throw exception when invitation token does not exist or expired")
    void shouldThrowWhenTokenInvalidOrExpired() {
        String invalidToken = "non-existent-token";
        when(invitationTokenRepository.findByToken(invalidToken)).thenReturn(Optional.empty());

        ActivateAccountRequest request = new ActivateAccountRequest(invalidToken, "SecretPassword123!");

        assertThrows(InvalidOrExpiredTokenException.class, () -> useCase.execute(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-102: Throw exception when token was already used")
    void shouldThrowWhenTokenAlreadyUsed() {
        UUID userId = UUID.randomUUID();
        String rawToken = "already-used-token";
        InvitationToken token = new InvitationToken(userId, rawToken, Instant.now().plus(10, ChronoUnit.HOURS));
        token.markAsUsed(); // already used

        when(invitationTokenRepository.findByToken(rawToken)).thenReturn(Optional.of(token));

        ActivateAccountRequest request = new ActivateAccountRequest(rawToken, "SecretPassword123!");

        assertThrows(InvalidOrExpiredTokenException.class, () -> useCase.execute(request));
        verify(userRepository, never()).save(any());
    }
}
