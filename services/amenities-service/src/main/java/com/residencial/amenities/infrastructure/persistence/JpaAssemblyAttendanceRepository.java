package com.residencial.amenities.infrastructure.persistence;

import com.residencial.amenities.domain.model.AssemblyAttendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaAssemblyAttendanceRepository extends JpaRepository<AssemblyAttendance, UUID> {
    List<AssemblyAttendance> findByAssemblyId(UUID assemblyId);
    Optional<AssemblyAttendance> findByAssemblyIdAndHouseId(UUID assemblyId, UUID houseId);
    long countByAssemblyIdAndIsPresentTrue(UUID assemblyId);
}
