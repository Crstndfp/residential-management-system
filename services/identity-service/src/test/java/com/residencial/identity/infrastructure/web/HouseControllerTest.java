package com.residencial.identity.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.identity.application.dto.CreateTenantRequest;
import com.residencial.identity.application.dto.CreateTenantResponse;
import com.residencial.identity.application.dto.RegisterHouseWithOwnerRequest;
import com.residencial.identity.application.dto.RegisterHouseWithOwnerResponse;
import com.residencial.identity.application.usecase.CreateTenantUseCase;
import com.residencial.identity.application.usecase.RegisterHouseWithOwnerUseCase;
import com.residencial.identity.domain.exception.EmailAlreadyInUseException;
import com.residencial.identity.domain.exception.HouseAlreadyExistsException;
import com.residencial.identity.domain.exception.HouseAlreadyHasOwnerException;
import com.residencial.identity.domain.exception.HouseAlreadyHasTenantException;
import com.residencial.identity.domain.exception.HouseNotFoundException;
import com.residencial.identity.domain.model.IdentificationType;
import com.residencial.identity.domain.model.ResidentialRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class HouseControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private RegisterHouseWithOwnerUseCase registerHouseWithOwnerUseCase;

    @Mock
    private CreateTenantUseCase createTenantUseCase;

    @InjectMocks
    private HouseController houseController;

    private static final String VALID_DPI = "1234567820101";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(houseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // =========================================================================
    // US-101: House Registration & Owner Onboarding Unit Tests
    // =========================================================================

    @Test
    @DisplayName("TC-101-01: Register house with valid Guatemalan CUI (Modulo 11) returns 201 Created")
    void testRegisterHouseWithOwnerSuccess() throws Exception {
        UUID houseId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(48, ChronoUnit.HOURS);

        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-10 L-01",
                "Avenida Los Proceres",
                IdentificationType.DPI,
                VALID_DPI,
                "owner.m1001@example.com",
                "Alejandro Morales"
        );

        RegisterHouseWithOwnerResponse response = new RegisterHouseWithOwnerResponse(
                houseId,
                "M-10 L-01",
                "Avenida Los Proceres",
                ownerId,
                "Alejandro Morales",
                "owner.m1001@example.com",
                "secret-invitation-token-123456",
                expiresAt,
                now
        );

        when(registerHouseWithOwnerUseCase.execute(any(RegisterHouseWithOwnerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/identity/houses/register-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.houseId", is(houseId.toString())))
                .andExpect(jsonPath("$.houseNumber", is("M-10 L-01")))
                .andExpect(jsonPath("$.streetOrBlock", is("Avenida Los Proceres")))
                .andExpect(jsonPath("$.ownerFullName", is("Alejandro Morales")))
                .andExpect(jsonPath("$.ownerEmail", is("owner.m1001@example.com")))
                .andExpect(jsonPath("$.invitationToken", is("secret-invitation-token-123456")));

        verify(registerHouseWithOwnerUseCase, times(1)).execute(any(RegisterHouseWithOwnerRequest.class));
    }

    @Test
    @DisplayName("TC-101-02: Reject duplicate house registration returns 400 Bad Request with HOUSE_ALREADY_EXISTS")
    void testRejectDuplicateHouseRegistration() throws Exception {
        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-10 L-01",
                "Avenida Los Proceres",
                IdentificationType.DPI,
                VALID_DPI,
                "another.owner@example.com",
                "Carlos Mendez"
        );

        when(registerHouseWithOwnerUseCase.execute(any(RegisterHouseWithOwnerRequest.class)))
                .thenThrow(new HouseAlreadyExistsException("M-10 L-01"));

        mockMvc.perform(post("/api/v1/identity/houses/register-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Domain Rule Violation")))
                .andExpect(jsonPath("$.errorCode", is("HOUSE_ALREADY_EXISTS")))
                .andExpect(jsonPath("$.detail", containsString("M-10 L-01")));
    }

    @Test
    @DisplayName("TC-101-03: Reject invalid CUI checksum returns 400 Bad Request with Invalid Argument")
    void testRejectInvalidCuiChecksum() throws Exception {
        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-10 L-02",
                "Avenida Los Proceres",
                IdentificationType.DPI,
                "1234567890101",
                "invalid.cui@example.com",
                "Felipe Ruiz"
        );

        when(registerHouseWithOwnerUseCase.execute(any(RegisterHouseWithOwnerRequest.class)))
                .thenThrow(new IllegalArgumentException("Invalid DPI/CUI checksum (Modulo 11 verification failed)."));

        mockMvc.perform(post("/api/v1/identity/houses/register-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Invalid Argument")))
                .andExpect(jsonPath("$.detail", containsString("Modulo 11 verification failed")));
    }

    @Test
    @DisplayName("TC-101-04: Register foreign owner with Passport returns 201 Created")
    void testRegisterForeignOwnerWithPassportSuccess() throws Exception {
        UUID houseId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        Instant now = Instant.now();
        Instant expiresAt = now.plus(48, ChronoUnit.HOURS);

        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-10 L-03",
                "Avenida Los Proceres",
                IdentificationType.PASSPORT,
                "US99887766",
                "foreign.owner@example.com",
                "Sarah Jenkins"
        );

        RegisterHouseWithOwnerResponse response = new RegisterHouseWithOwnerResponse(
                houseId,
                "M-10 L-03",
                "Avenida Los Proceres",
                ownerId,
                "Sarah Jenkins",
                "foreign.owner@example.com",
                "passport-invitation-token-789",
                expiresAt,
                now
        );

        when(registerHouseWithOwnerUseCase.execute(any(RegisterHouseWithOwnerRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/identity/houses/register-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.houseId", is(houseId.toString())))
                .andExpect(jsonPath("$.ownerFullName", is("Sarah Jenkins")))
                .andExpect(jsonPath("$.invitationToken", is("passport-invitation-token-789")));
    }

    @Test
    @DisplayName("US-101 Invariant: Reject registering a second owner on the same house returns 400 Bad Request")
    void testRejectSecondOwnerOnSameHouse() throws Exception {
        RegisterHouseWithOwnerRequest request = new RegisterHouseWithOwnerRequest(
                "M-10 L-01",
                "Avenida Los Proceres",
                IdentificationType.DPI,
                VALID_DPI,
                "second.owner@example.com",
                "Maria Gonzalez"
        );

        when(registerHouseWithOwnerUseCase.execute(any(RegisterHouseWithOwnerRequest.class)))
                .thenThrow(new HouseAlreadyHasOwnerException("M-10 L-01"));

        mockMvc.perform(post("/api/v1/identity/houses/register-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("HOUSE_ALREADY_HAS_OWNER")))
                .andExpect(jsonPath("$.title", is("Domain Rule Violation")));
    }

    @Test
    @DisplayName("US-101 Validation: Reject registration when required fields are missing or invalid")
    void testRejectInvalidHouseRegistrationPayload() throws Exception {
        RegisterHouseWithOwnerRequest invalidRequest = new RegisterHouseWithOwnerRequest(
                "", // Blank house number
                "", // Blank street
                null, // Null idType
                "", // Blank idNumber
                "not-an-email", // Invalid email
                "" // Blank full name
        );

        mockMvc.perform(post("/api/v1/identity/houses/register-owner")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Error")))
                .andExpect(jsonPath("$.errors.houseNumber").exists())
                .andExpect(jsonPath("$.errors.streetOrBlock").exists())
                .andExpect(jsonPath("$.errors.idType").exists())
                .andExpect(jsonPath("$.errors.idNumber").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());

        verify(registerHouseWithOwnerUseCase, never()).execute(any());
    }

    // =========================================================================
    // US-102: Tenant Creation Unit Tests
    // =========================================================================

    @Test
    @DisplayName("TC-102-03: Owner creates tenant for the house returns 201 Created")
    void testCreateTenantSuccess() throws Exception {
        UUID houseId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        Instant expiresAt = Instant.now().plus(48, ChronoUnit.HOURS);

        CreateTenantRequest request = new CreateTenantRequest(
                IdentificationType.PASSPORT,
                "FR11223344",
                "tenant.m1001@example.com",
                "Jean Dupont"
        );

        CreateTenantResponse response = new CreateTenantResponse(
                tenantId,
                houseId,
                "M-10 L-01",
                "tenant.m1001@example.com",
                "Jean Dupont",
                ResidentialRole.TENANT,
                "tenant-invitation-token-abc",
                expiresAt,
                Instant.now()
        );

        when(createTenantUseCase.execute(eq(houseId), any(CreateTenantRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/identity/houses/{houseId}/tenants", houseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tenantId", is(tenantId.toString())))
                .andExpect(jsonPath("$.houseId", is(houseId.toString())))
                .andExpect(jsonPath("$.fullName", is("Jean Dupont")))
                .andExpect(jsonPath("$.email", is("tenant.m1001@example.com")))
                .andExpect(jsonPath("$.residentialRole", is("TENANT")))
                .andExpect(jsonPath("$.invitationToken", is("tenant-invitation-token-abc")));

        verify(createTenantUseCase, times(1)).execute(eq(houseId), any(CreateTenantRequest.class));
    }

    @Test
    @DisplayName("TC-102-04: Invariant: Reject creating a second tenant on the same house returns 400 Bad Request")
    void testRejectSecondTenantOnSameHouse() throws Exception {
        UUID houseId = UUID.randomUUID();
        CreateTenantRequest request = new CreateTenantRequest(
                IdentificationType.PASSPORT,
                "DE55667788",
                "second.tenant@example.com",
                "Hans Schmidt"
        );

        when(createTenantUseCase.execute(eq(houseId), any(CreateTenantRequest.class)))
                .thenThrow(new HouseAlreadyHasTenantException("M-10 L-01"));

        mockMvc.perform(post("/api/v1/identity/houses/{houseId}/tenants", houseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Domain Rule Violation")))
                .andExpect(jsonPath("$.errorCode", is("HOUSE_ALREADY_HAS_TENANT")))
                .andExpect(jsonPath("$.detail", containsString("M-10 L-01")));
    }

    @Test
    @DisplayName("US-102: Reject creating tenant for non-existent house returns 400 Bad Request")
    void testRejectTenantWhenHouseNotFound() throws Exception {
        UUID nonExistentHouseId = UUID.randomUUID();
        CreateTenantRequest request = new CreateTenantRequest(
                IdentificationType.DPI,
                VALID_DPI,
                "tenant@example.com",
                "Pedro Gomez"
        );

        when(createTenantUseCase.execute(eq(nonExistentHouseId), any(CreateTenantRequest.class)))
                .thenThrow(new HouseNotFoundException(nonExistentHouseId));

        mockMvc.perform(post("/api/v1/identity/houses/{houseId}/tenants", nonExistentHouseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("HOUSE_NOT_FOUND")))
                .andExpect(jsonPath("$.title", is("Domain Rule Violation")));
    }

    @Test
    @DisplayName("US-102: Reject creating tenant when email is already registered returns 400 Bad Request")
    void testRejectTenantWhenEmailAlreadyInUse() throws Exception {
        UUID houseId = UUID.randomUUID();
        CreateTenantRequest request = new CreateTenantRequest(
                IdentificationType.PASSPORT,
                "IT998877",
                "existing@example.com",
                "Marco Rossi"
        );

        when(createTenantUseCase.execute(eq(houseId), any(CreateTenantRequest.class)))
                .thenThrow(new EmailAlreadyInUseException("existing@example.com"));

        mockMvc.perform(post("/api/v1/identity/houses/{houseId}/tenants", houseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode", is("EMAIL_ALREADY_IN_USE")))
                .andExpect(jsonPath("$.title", is("Domain Rule Violation")));
    }

    @Test
    @DisplayName("US-102 Validation: Reject tenant request with invalid payload")
    void testRejectInvalidTenantPayload() throws Exception {
        UUID houseId = UUID.randomUUID();
        CreateTenantRequest invalidRequest = new CreateTenantRequest(
                null,
                "",
                "bad-email",
                ""
        );

        mockMvc.perform(post("/api/v1/identity/houses/{houseId}/tenants", houseId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Error")))
                .andExpect(jsonPath("$.errors.idType").exists())
                .andExpect(jsonPath("$.errors.idNumber").exists())
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.fullName").exists());

        verify(createTenantUseCase, never()).execute(any(), any());
    }
}
