package com.residencial.identity.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.identity.application.dto.ActivateAccountRequest;
import com.residencial.identity.application.dto.ActivateAccountResponse;
import com.residencial.identity.application.usecase.ActivateAccountUseCase;
import com.residencial.identity.domain.exception.InvalidOrExpiredTokenException;
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
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ActivateAccountUseCase activateAccountUseCase;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    @DisplayName("TC-102-01: Activate account with valid invitation token returns 200 OK")
    void testActivateAccountSuccess() throws Exception {
        UUID userId = UUID.randomUUID();
        String validToken = "valid-token-123456789";
        ActivateAccountRequest request = new ActivateAccountRequest(validToken, "SuperSecurePassword123!");

        ActivateAccountResponse response = new ActivateAccountResponse(
                userId,
                "owner@example.com",
                "Alejandro Morales",
                ResidentialRole.OWNER,
                true,
                Instant.now()
        );

        when(activateAccountUseCase.execute(any(ActivateAccountRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/identity/users/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId", is(userId.toString())))
                .andExpect(jsonPath("$.email", is("owner@example.com")))
                .andExpect(jsonPath("$.fullName", is("Alejandro Morales")))
                .andExpect(jsonPath("$.residentialRole", is("OWNER")))
                .andExpect(jsonPath("$.isActive", is(true)));

        verify(activateAccountUseCase, times(1)).execute(any(ActivateAccountRequest.class));
    }

    @Test
    @DisplayName("TC-102-02: Reject reusing already consumed or expired token returns 400 Bad Request with INVALID_OR_EXPIRED_TOKEN")
    void testRejectReusingConsumedOrExpiredToken() throws Exception {
        String consumedToken = "already-used-or-expired-token";
        ActivateAccountRequest request = new ActivateAccountRequest(consumedToken, "AnotherPassword123!");

        when(activateAccountUseCase.execute(any(ActivateAccountRequest.class)))
                .thenThrow(new InvalidOrExpiredTokenException());

        mockMvc.perform(post("/api/v1/identity/users/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Domain Rule Violation")))
                .andExpect(jsonPath("$.errorCode", is("INVALID_OR_EXPIRED_TOKEN")))
                .andExpect(jsonPath("$.detail", containsString("invalid, has expired, or was already used")));
    }

    @Test
    @DisplayName("US-102 Validation: Reject activation when token is blank or password is too short")
    void testRejectInvalidActivationPayload() throws Exception {
        ActivateAccountRequest invalidRequest = new ActivateAccountRequest(
                "", // Blank token
                "123" // Password too short (< 8 chars)
        );

        mockMvc.perform(post("/api/v1/identity/users/activate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("Validation Error")))
                .andExpect(jsonPath("$.errors.token").exists())
                .andExpect(jsonPath("$.errors.password").exists());

        verify(activateAccountUseCase, never()).execute(any());
    }
}
