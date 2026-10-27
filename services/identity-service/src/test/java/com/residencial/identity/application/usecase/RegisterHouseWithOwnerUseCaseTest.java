package com.residencial.identity.application.usecase;

import com.residencial.identity.application.dto.RegisterHouseWithOwnerRequest;
import com.residencial.identity.application.dto.RegisterHouseWithOwnerResponse;
import com.residencial.identity.domain.exception.EmailAlreadyInUseException;
import com.residencial.identity.domain.exception.HouseAlreadyExistsException;
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
class RegisterHouseWithOwnerUseCaseTest {

    @Mock
    private HouseRepository houseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private InvitationTokenRepository invitationTokenRepository;

    private RegisterHouseWithOwnerUseCase useCase;

    // Valid Guatemalan CUI checksum test
    private static final String VALID_DPI = "1234567820101";

    @BeforeEach
    void setUp() {
        useCase = new RegisterHouseWithOwnerUseCase(houseRepository, userRepository, invitationTokenRepository);
    }

    @Test
    @DisplayName("US-101: Successfully register house with owner and generate 48-hour invitation token")
    void shouldRegisterHouseWithOwnerSuccessfully() {
        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-01 L-05",
                "Main Boulevard",
                IdentificationType.DPI,
                VALID_DPI,
                "owner@example.com",
                "Juan Perez"
        );

        when(houseRepository.existsByHouseNumber("M-01 L-05")).thenReturn(false);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.empty());

        House savedHouse = new House("M-01 L-05", "Main Boulevard");
        when(houseRepository.save(any(House.class))).thenReturn(savedHouse);
        when(userRepository.existsByHouseIdAndResidentialRole(any(), eq(ResidentialRole.OWNER))).thenReturn(false);

        IdentificationDocument doc = new IdentificationDocument(IdentificationType.DPI, VALID_DPI);
        User savedUser = new User(savedHouse, doc, "owner@example.com", "Juan Perez", ResidentialRole.OWNER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(invitationTokenRepository.save(any(InvitationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterHouseWithOwnerResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("M-01 L-05", response.houseNumber());
        assertEquals("owner@example.com", response.ownerEmail());
        assertEquals("Juan Perez", response.ownerFullName());
        assertNotNull(response.invitationToken());
        assertNotNull(response.tokenExpiresAt());

        verify(houseRepository).save(any(House.class));
        verify(userRepository).save(any(User.class));
        verify(invitationTokenRepository).save(any(InvitationToken.class));
    }

    @Test
    @DisplayName("US-101: Throw exception when house number already exists")
    void shouldThrowWhenHouseNumberAlreadyExists() {
        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-01 L-05",
                "Main Boulevard",
                IdentificationType.DPI,
                VALID_DPI,
                "owner@example.com",
                "Juan Perez"
        );

        when(houseRepository.existsByHouseNumber("M-01 L-05")).thenReturn(true);

        HouseAlreadyExistsException ex = assertThrows(
                HouseAlreadyExistsException.class,
                () -> useCase.execute(request)
        );

        assertEquals("HOUSE_ALREADY_EXISTS", ex.getErrorCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-101: Throw exception when owner email is already in use")
    void shouldThrowWhenEmailAlreadyInUse() {
        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-01 L-06",
                "Main Boulevard",
                IdentificationType.DPI,
                VALID_DPI,
                "owner@example.com",
                "Juan Perez"
        );

        when(houseRepository.existsByHouseNumber("M-01 L-06")).thenReturn(false);
        when(userRepository.findByEmail("owner@example.com")).thenReturn(Optional.of(mock(User.class)));

        EmailAlreadyInUseException ex = assertThrows(
                EmailAlreadyInUseException.class,
                () -> useCase.execute(request)
        );

        assertEquals("EMAIL_ALREADY_IN_USE", ex.getErrorCode());
        verify(houseRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-101: Throw exception when DPI checksum is invalid")
    void shouldThrowWhenDpiChecksumIsInvalid() {
        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-01 L-07",
                "Main Boulevard",
                IdentificationType.DPI,
                "1234567890101", // Invalid checksum verifier
                "owner2@example.com",
                "Maria Lopez"
        );

        when(houseRepository.existsByHouseNumber("M-01 L-07")).thenReturn(false);
        when(userRepository.findByEmail("owner2@example.com")).thenReturn(Optional.empty());
        when(houseRepository.save(any(House.class))).thenReturn(new House("M-01 L-07", "Main Boulevard"));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> useCase.execute(request)
        );

        assertTrue(ex.getMessage().contains("Modulo 11 verification failed"));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("US-101: Successfully register house with foreign owner using Passport")
    void shouldRegisterWithPassportSuccessfully() {
        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-02 L-01",
                "Boulevard Norte",
                IdentificationType.PASSPORT,
                "F88992211",
                "foreign.owner@example.com",
                "Emily Watson"
        );

        when(houseRepository.existsByHouseNumber("M-02 L-01")).thenReturn(false);
        when(userRepository.findByEmail("foreign.owner@example.com")).thenReturn(Optional.empty());

        House savedHouse = new House("M-02 L-01", "Boulevard Norte");
        when(houseRepository.save(any(House.class))).thenReturn(savedHouse);

        IdentificationDocument doc = new IdentificationDocument(IdentificationType.PASSPORT, "F88992211");
        User savedUser = new User(savedHouse, doc, "foreign.owner@example.com", "Emily Watson", ResidentialRole.OWNER);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        when(invitationTokenRepository.save(any(InvitationToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisterHouseWithOwnerResponse response = useCase.execute(request);

        assertNotNull(response);
        assertEquals("Emily Watson", response.ownerFullName());
        assertEquals("foreign.owner@example.com", response.ownerEmail());
        assertNotNull(response.invitationToken());
    }
}
