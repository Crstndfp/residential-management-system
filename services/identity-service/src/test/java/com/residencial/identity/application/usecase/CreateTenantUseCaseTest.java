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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateTenantUseCaseTest {

    @Mock
    private HouseRepository houseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvitationTokenRepository invitationTokenRepository;

    private CreateTenantUseCase useCase;

    // Valid Guatemalan DPI sample
    private static final String VALID_DPI = "1234567820101";

    @BeforeEach
    void setUp() {
        useCase = new CreateTenantUseCase(houseRepository, userRepository, invitationTokenRepository);
    }

    @Test
    @DisplayName("US-102: Successfully create tenant for a house and generate 48h invitation token")
    void shouldCreateTenantSuccessfully() {
        UUID houseId = UUID.randomUUID();
        House house = new House("M-03 L-10", "Avenida Real");

        CreateTenantRequest request = new CreateTenantRequest(
                IdentificationType.DPI,
                VALID_DPI,
                "tenant@example.com",
                "Pedro Gomez"
        );

        when(houseRepository.findById(house.getId())).thenReturn(Optional.of(house));
        when(userRepository.existsByHouseIdAndResidentialRole(house.getId(), ResidentialRole.TENANT)).thenReturn(false);
        when(userRepository.findByEmail("tenant@example.com")).thenReturn(Optional.empty());

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(invitationTokenRepository.save(any(InvitationToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CreateTenantResponse response = useCase.execute(house.getId(), request);

        assertNotNull(response);
        assertEquals(house.getId(), response.houseId());
        assertEquals("tenant@example.com", response.email());
        assertEquals(ResidentialRole.TENANT, response.residentialRole());
        assertNotNull(response.invitationToken());
        assertNotNull(response.tokenExpiresAt());

        verify(userRepository).save(any(User.class));
        verify(invitationTokenRepository).save(any(InvitationToken.class));
    }

    @Test
    @DisplayName("US-102: Invariant enforcement: Throw exception when house already has a tenant")
    void shouldThrowWhenHouseAlreadyHasTenant() {
        UUID houseId = UUID.randomUUID();
        House house = new House("M-03 L-10", "Avenida Real");

        CreateTenantRequest request = new CreateTenantRequest(
                IdentificationType.DPI,
                VALID_DPI,
                "tenant2@example.com",
                "Ana Ruiz"
        );

        when(houseRepository.findById(houseId)).thenReturn(Optional.of(house));
        when(userRepository.existsByHouseIdAndResidentialRole(houseId, ResidentialRole.TENANT)).thenReturn(true);

        HouseAlreadyHasTenantException ex = assertThrows(
                HouseAlreadyHasTenantException.class,
                () -> useCase.execute(houseId, request)
        );

        assertEquals("HOUSE_ALREADY_HAS_TENANT", ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-102: Throw exception when tenant email is already in use")
    void shouldThrowWhenTenantEmailAlreadyInUse() {
        UUID houseId = UUID.randomUUID();
        House house = new House("M-03 L-10", "Avenida Real");

        CreateTenantRequest request = new CreateTenantRequest(
                IdentificationType.PASSPORT,
                "P11223344",
                "existing@example.com",
                "Maria Lopez"
        );

        when(houseRepository.findById(houseId)).thenReturn(Optional.of(house));
        when(userRepository.existsByHouseIdAndResidentialRole(houseId, ResidentialRole.TENANT)).thenReturn(false);
        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(mock(User.class)));

        EmailAlreadyInUseException ex = assertThrows(
                EmailAlreadyInUseException.class,
                () -> useCase.execute(houseId, request)
        );

        assertEquals("EMAIL_ALREADY_IN_USE", ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-102: Throw exception when house does not exist")
    void shouldThrowWhenHouseNotFound() {
        UUID nonExistentHouseId = UUID.randomUUID();
        CreateTenantRequest request = new CreateTenantRequest(
                IdentificationType.DPI,
                VALID_DPI,
                "tenant@example.com",
                "Pedro Gomez"
        );

        when(houseRepository.findById(nonExistentHouseId)).thenReturn(Optional.empty());

        assertThrows(HouseNotFoundException.class, () -> useCase.execute(nonExistentHouseId, request));
        verify(userRepository, never()).save(any());
    }
}
