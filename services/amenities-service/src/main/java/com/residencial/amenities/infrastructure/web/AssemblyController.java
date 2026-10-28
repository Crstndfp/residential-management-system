package com.residencial.amenities.infrastructure.web;

import com.residencial.amenities.application.dto.*;
import com.residencial.amenities.application.usecase.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/amenities/assemblies")
public class AssemblyController {

    private final ConvokeAssemblyUseCase convokeAssemblyUseCase;
    private final RegisterAttendanceUseCase registerAttendanceUseCase;
    private final GetAssemblyQuorumUseCase getAssemblyQuorumUseCase;
    private final CreateMotionUseCase createMotionUseCase;

    public AssemblyController(ConvokeAssemblyUseCase convokeAssemblyUseCase,
                              RegisterAttendanceUseCase registerAttendanceUseCase,
                              GetAssemblyQuorumUseCase getAssemblyQuorumUseCase,
                              CreateMotionUseCase createMotionUseCase) {
        this.convokeAssemblyUseCase = convokeAssemblyUseCase;
        this.registerAttendanceUseCase = registerAttendanceUseCase;
        this.getAssemblyQuorumUseCase = getAssemblyQuorumUseCase;
        this.createMotionUseCase = createMotionUseCase;
    }

    @PostMapping
    public ResponseEntity<AssemblyResponse> convokeAssembly(@Valid @RequestBody ConvokeAssemblyRequest request) {
        AssemblyResponse response = convokeAssemblyUseCase.execute(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/attendance")
    public ResponseEntity<AttendanceResponse> registerAttendance(
            @PathVariable UUID id,
            @Valid @RequestBody RegisterAttendanceRequest request) {
        AttendanceResponse response = registerAttendanceUseCase.execute(id, request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/quorum")
    public ResponseEntity<AssemblyQuorumResponse> getQuorum(@PathVariable UUID id) {
        AssemblyQuorumResponse response = getAssemblyQuorumUseCase.execute(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/motions")
    public ResponseEntity<MotionResponse> createMotion(
            @PathVariable UUID id,
            @Valid @RequestBody CreateMotionRequest request) {
        MotionResponse response = createMotionUseCase.execute(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
