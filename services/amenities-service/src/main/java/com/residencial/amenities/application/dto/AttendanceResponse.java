package com.residencial.amenities.application.dto;

import com.residencial.amenities.domain.model.AssemblyAttendance;

import java.time.Instant;
import java.util.UUID;

public record AttendanceResponse(
        UUID id,
        UUID assemblyId,
        UUID houseId,
        String houseNumber,
        UUID attendedByUserId,
        String attendedByName,
        boolean isPresent,
        Instant checkedInAt
) {
    public static AttendanceResponse fromDomain(AssemblyAttendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getAssemblyId(),
                attendance.getHouseId(),
                attendance.getHouseNumber(),
                attendance.getAttendedByUserId(),
                attendance.getAttendedByName(),
                attendance.isPresent(),
                attendance.getCheckedInAt()
        );
    }
}
