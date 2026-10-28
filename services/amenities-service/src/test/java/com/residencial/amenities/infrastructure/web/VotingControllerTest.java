package com.residencial.amenities.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.residencial.amenities.application.dto.CastVoteRequest;
import com.residencial.amenities.application.dto.CloseMotionResponse;
import com.residencial.amenities.application.dto.MotionResponse;
import com.residencial.amenities.application.dto.VoteResponse;
import com.residencial.amenities.application.usecase.CastMotionVoteUseCase;
import com.residencial.amenities.application.usecase.CloseMotionUseCase;
import com.residencial.amenities.application.usecase.OpenMotionUseCase;
import com.residencial.amenities.domain.exception.DuplicateVoteException;
import com.residencial.amenities.domain.exception.TenantVotingForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {VotingController.class, GlobalExceptionHandler.class})
class VotingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OpenMotionUseCase openMotionUseCase;

    @MockBean
    private CastMotionVoteUseCase castMotionVoteUseCase;

    @MockBean
    private CloseMotionUseCase closeMotionUseCase;

    @Test
    @DisplayName("POST /api/v1/amenities/motions/{id}/open returns 200 OK")
    void openMotionReturns200() throws Exception {
        UUID motionId = UUID.randomUUID();
        MotionResponse response = new MotionResponse(
                motionId,
                UUID.randomUUID(),
                "Budget Motion",
                "Approve budget",
                "OPEN",
                null,
                Instant.now(),
                null,
                Instant.now()
        );

        when(openMotionUseCase.execute(motionId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/amenities/motions/{id}/open", motionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(motionId.toString()))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    @DisplayName("POST /api/v1/amenities/motions/{id}/vote returns 201 Created for OWNER")
    void castVoteOwnerReturns201() throws Exception {
        UUID motionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        CastVoteRequest request = new CastVoteRequest(
                houseId,
                "B-101",
                userId,
                "IN_FAVOR"
        );

        VoteResponse response = new VoteResponse(
                UUID.randomUUID(),
                motionId,
                houseId,
                "B-101",
                userId,
                "IN_FAVOR",
                "abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123456789",
                Instant.now()
        );

        when(castMotionVoteUseCase.execute(eq(motionId), any(CastVoteRequest.class), eq("OWNER"))).thenReturn(response);

        mockMvc.perform(post("/api/v1/amenities/motions/{id}/vote", motionId)
                        .header("X-User-Role", "OWNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.motionId").value(motionId.toString()))
                .andExpect(jsonPath("$.voteChoice").value("IN_FAVOR"))
                .andExpect(jsonPath("$.voteHash").isNotEmpty());
    }

    @Test
    @DisplayName("POST /api/v1/amenities/motions/{id}/vote returns 403 Forbidden for TENANT")
    void castVoteTenantReturns403() throws Exception {
        UUID motionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();

        CastVoteRequest request = new CastVoteRequest(
                houseId,
                "B-101",
                UUID.randomUUID(),
                "IN_FAVOR"
        );

        when(castMotionVoteUseCase.execute(eq(motionId), any(CastVoteRequest.class), eq("TENANT")))
                .thenThrow(new TenantVotingForbiddenException("TENANT"));

        mockMvc.perform(post("/api/v1/amenities/motions/{id}/vote", motionId)
                        .header("X-User-Role", "TENANT")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.title").value("Tenant Voting Forbidden"))
                .andExpect(jsonPath("$.errorCode").value("TENANT_VOTING_FORBIDDEN"));
    }

    @Test
    @DisplayName("POST /api/v1/amenities/motions/{id}/vote returns 409 Conflict on duplicate vote")
    void castDuplicateVoteReturns409() throws Exception {
        UUID motionId = UUID.randomUUID();
        UUID houseId = UUID.randomUUID();

        CastVoteRequest request = new CastVoteRequest(
                houseId,
                "B-101",
                UUID.randomUUID(),
                "IN_FAVOR"
        );

        when(castMotionVoteUseCase.execute(eq(motionId), any(CastVoteRequest.class), eq("OWNER")))
                .thenThrow(new DuplicateVoteException(motionId, houseId));

        mockMvc.perform(post("/api/v1/amenities/motions/{id}/vote", motionId)
                        .header("X-User-Role", "OWNER")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate Vote Cast"))
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_VOTE_CAST"));
    }

    @Test
    @DisplayName("POST /api/v1/amenities/motions/{id}/close returns 200 OK with tally and verdict")
    void closeMotionReturns200() throws Exception {
        UUID motionId = UUID.randomUUID();
        CloseMotionResponse response = new CloseMotionResponse(
                motionId,
                "Roof Maintenance",
                "CLOSED",
                "APPROVED",
                25L,
                5L,
                2L,
                32L,
                78.13,
                Instant.now()
        );

        when(closeMotionUseCase.execute(motionId)).thenReturn(response);

        mockMvc.perform(post("/api/v1/amenities/motions/{id}/close", motionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motionId").value(motionId.toString()))
                .andExpect(jsonPath("$.status").value("CLOSED"))
                .andExpect(jsonPath("$.verdict").value("APPROVED"))
                .andExpect(jsonPath("$.approvalPercentage").value(78.13));
    }
}
