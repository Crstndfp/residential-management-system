package com.residencial.identity.application.usecase;

import com.residencial.identity.application.dto.RegisterHouseWithOwnerRequest;
import com.residencial.identity.application.dto.RegisterHouseWithOwnerResponse;
import com.residencial.identity.domain.exception.EmailAlreadyInUseException;
import com.residencial.identity.domain.exception.HouseAlreadyExistsException;
import com.residencial.identity.domain.exception.HouseAlreadyHasOwnerException;
import com.residencial.identity.domain.model.*;
import com.residencial.identity.domain.repository.HouseRepository;
import com.residencial.identity.domain.repository.InvitationTokenRepository;
import com.residencial.identity.domain.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
@Transactional
public class RegisterHouseWithOwnerUseCase {

    private final HouseRepository houseRepository;
    private final UserRepository userRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public RegisterHouseWithOwnerUseCase(
            HouseRepository houseRepository,
            UserRepository userRepository,
            InvitationTokenRepository invitationTokenRepository) {
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
        this.invitationTokenRepository = invitationTokenRepository;
    }

    public RegisterHouseWithOwnerResponse execute(RegisterHouseWithOwnerRequest request) {
        // 1. Enforce unique house number
        if (houseRepository.existsByHouseNumber(request.houseNumber())) {
            throw new HouseAlreadyExistsException(request.houseNumber());
        }

        // 2. Enforce unique user email
        if (userRepository.findByEmail(request.email().trim().toLowerCase()).isPresent()) {
            throw new EmailAlreadyInUseException(request.email());
        }

        // 3. Persist House entity
        House house = houseRepository.save(new House(request.houseNumber(), request.streetOrBlock()));

        // 4. Invariant: only 1 OWNER per house
        if (userRepository.existsByHouseIdAndResidentialRole(house.getId(), ResidentialRole.OWNER)) {
            throw new HouseAlreadyHasOwnerException(house.getHouseNumber());
        }

        // 5. Construct Value Object with Modulo 11 validation (renap CUI or passport)
        IdentificationDocument idDoc = new IdentificationDocument(request.idType(), request.idNumber());

        // 6. Create and persist User with role OWNER
        User owner = new User(house, idDoc, request.email(), request.fullName(), ResidentialRole.OWNER);
        User savedOwner = userRepository.save(owner);

        // 7. Generate secure 48-hour invitation token for onboarding
        String rawToken = generateSecureToken();
        Instant expiresAt = Instant.now().plus(48, ChronoUnit.HOURS);
        InvitationToken invitationToken = invitationTokenRepository.save(
                new InvitationToken(savedOwner.getId(), rawToken, expiresAt)
        );

        return new RegisterHouseWithOwnerResponse(
                house.getId(),
                house.getHouseNumber(),
                house.getStreetOrBlock(),
                savedOwner.getId(),
                savedOwner.getFullName(),
                savedOwner.getEmail(),
                invitationToken.getToken(),
                invitationToken.getExpiresAt(),
                savedOwner.getCreatedAt()
        );
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
