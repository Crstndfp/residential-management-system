package com.residencial.amenities.domain.repository;

import com.residencial.amenities.domain.model.AssemblyAttendance;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AssemblyAttendanceRepository {
    AssemblyAttendance save(AssemblyAttendance attendance);
    List<AssemblyAttendance> findByAssemblyId(UUID assemblyId);
    Optional<AssemblyAttendance> findByAssemblyIdAndHouseId(UUID assemblyId, UUID houseId);
    long countByAssemblyIdAndIsPresentTrue(UUID assemblyId);
}
