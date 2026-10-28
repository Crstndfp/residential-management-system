package com.residencial.amenities.application.usecase;

import com.residencial.amenities.application.dto.AttendanceResponse;
import com.residencial.amenities.application.dto.RegisterAttendanceRequest;
import com.residencial.amenities.domain.exception.AssemblyNotFoundException;
import com.residencial.amenities.domain.model.AssemblyAttendance;
import com.residencial.amenities.domain.repository.AssemblyAttendanceRepository;
import com.residencial.amenities.domain.repository.AssemblyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
public class RegisterAttendanceUseCase {

    private final AssemblyRepository assemblyRepository;
    private final AssemblyAttendanceRepository attendanceRepository;

    public RegisterAttendanceUseCase(AssemblyRepository assemblyRepository,
                                    AssemblyAttendanceRepository attendanceRepository) {
        this.assemblyRepository = assemblyRepository;
        this.attendanceRepository = attendanceRepository;
    }

    @Transactional
    public AttendanceResponse execute(UUID assemblyId, RegisterAttendanceRequest request) {
        assemblyRepository.findById(assemblyId)
                .orElseThrow(() -> new AssemblyNotFoundException(assemblyId));

        boolean isPresent = request.isPresent() == null || request.isPresent();

        AssemblyAttendance attendance = attendanceRepository
                .findByAssemblyIdAndHouseId(assemblyId, request.houseId())
                .map(existing -> new AssemblyAttendance(
                        existing.getId(),
                        assemblyId,
                        request.houseId(),
                        request.houseNumber(),
                        request.attendedByUserId(),
                        request.attendedByName(),
                        isPresent,
                        Instant.now()
                ))
                .orElseGet(() -> new AssemblyAttendance(
                        UUID.randomUUID(),
                        assemblyId,
                        request.houseId(),
                        request.houseNumber(),
                        request.attendedByUserId(),
                        request.attendedByName(),
                        isPresent,
                        Instant.now()
                ));

        AssemblyAttendance saved = attendanceRepository.save(attendance);
        return AttendanceResponse.fromDomain(saved);
    }
}
