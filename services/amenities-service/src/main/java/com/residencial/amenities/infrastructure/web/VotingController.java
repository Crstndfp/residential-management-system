package com.residencial.amenities.infrastructure.web;

import com.residencial.amenities.application.dto.CastVoteRequest;
import com.residencial.amenities.application.dto.CloseMotionResponse;
import com.residencial.amenities.application.dto.MotionResponse;
import com.residencial.amenities.application.dto.VoteResponse;
import com.residencial.amenities.application.usecase.CastMotionVoteUseCase;
import com.residencial.amenities.application.usecase.CloseMotionUseCase;
import com.residencial.amenities.application.usecase.OpenMotionUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/amenities/motions")
public class VotingController {

    private final OpenMotionUseCase openMotionUseCase;
    private final CastMotionVoteUseCase castMotionVoteUseCase;
    private final CloseMotionUseCase closeMotionUseCase;

    public VotingController(OpenMotionUseCase openMotionUseCase,
                            CastMotionVoteUseCase castMotionVoteUseCase,
                            CloseMotionUseCase closeMotionUseCase) {
        this.openMotionUseCase = openMotionUseCase;
        this.castMotionVoteUseCase = castMotionVoteUseCase;
        this.closeMotionUseCase = closeMotionUseCase;
    }

    @PostMapping("/{id}/open")
    public ResponseEntity<MotionResponse> openMotion(@PathVariable UUID id) {
        MotionResponse response = openMotionUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/vote")
    public ResponseEntity<VoteResponse> castVote(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Role", defaultValue = "OWNER") String userRole,
            @Valid @RequestBody CastVoteRequest request) {
        VoteResponse response = castMotionVoteUseCase.execute(id, request, userRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<CloseMotionResponse> closeMotion(@PathVariable UUID id) {
        CloseMotionResponse response = closeMotionUseCase.execute(id);
        return ResponseEntity.ok(response);
    }
}
