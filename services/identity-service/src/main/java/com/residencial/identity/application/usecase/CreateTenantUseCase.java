package com.residencial.identity.application.usecase;

import com.residencial.identity.application.dto.CreateTenantRequest;
import com.residencial.identity.application.dto.CreateTenantResponse;
import com.residencial.identity.domain.exception.EmailAlreadyInUseException;
import com.residencial.identity.domain.exception.HouseAlreadyHasTenantException;
import com.residencial.identity.domain.exception.HouseNotFoundException;
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
import java.util.UUID;

@Service
@Transactional
public class CreateTenantUseCase {

    private final HouseRepository houseRepository;
    private final UserRepository userRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public CreateTenantUseCase(
            HouseRepository houseRepository,
            UserRepository userRepository,
            InvitationTokenRepository invitationTokenRepository) {
        this.houseRepository = houseRepository;
        this.userRepository = userRepository;
        this.invitationTokenRepository = invitationTokenRepository;
    }

    public CreateTenantResponse execute(UUID houseId, CreateTenantRequest request) {
        House house = houseRepository.findById(houseId)
                .orElseThrow(() -> new HouseNotFoundException(houseId));

        // 1. Enforce max 1 TENANT per house invariant
        if (userRepository.existsByHouseIdAndResidentialRole(houseId, ResidentialRole.TENANT)) {
            throw new HouseAlreadyHasTenantException(house.getHouseNumber());
        }

        // 2. Enforce email uniqueness
        if (userRepository.findByEmail(request.email().trim().toLowerCase()).isPresent()) {
            throw new EmailAlreadyInUseException(request.email());
        }

        // 3. Identification Document validation (Modulo 11 or passport)
        IdentificationDocument idDoc = new IdentificationDocument(request.idType(), request.idNumber());

        // 4. Create User with role TENANT
        User tenant = new User(house, idDoc, request.email(), request.fullName(), ResidentialRole.TENANT);
        User savedTenant = userRepository.save(tenant);

        // 5. Generate 48-hour onboarding invitation token
        String rawToken = generateSecureToken();
        Instant expiresAt = Instant.now().plus(48, ChronoUnit.HOURS);
        InvitationToken invitationToken = invitationTokenRepository.save(
                new InvitationToken(savedTenant.getId(), rawToken, expiresAt)
        );

        return new CreateTenantResponse(
                savedTenant.getId(),
                house.getId(),
                house.getHouseNumber(),
                savedTenant.getEmail(),
                savedTenant.getFullName(),
                savedTenant.getResidentialRole(),
                invitationToken.getToken(),
                invitationToken.getExpiresAt(),
                savedTenant.getCreatedAt()
        );
    }

    private String generateSecureToken() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
