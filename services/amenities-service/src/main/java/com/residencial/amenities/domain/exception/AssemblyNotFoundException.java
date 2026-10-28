package com.residencial.amenities.domain.exception;

import java.util.UUID;

public class AssemblyNotFoundException extends AmenitiesDomainException {
    public AssemblyNotFoundException(UUID id) {
        super("ASSEMBLY_NOT_FOUND", "Assembly not found with ID: " + id);
    }
}
