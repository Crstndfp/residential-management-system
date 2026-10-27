package com.residencial.identity.application.dto;

import com.residencial.identity.domain.model.IdentificationType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RegisterHouseWithOwnerRequest(
        @NotBlank(message = "House number is required")
        @Size(max = 20, message = "House number cannot exceed 20 characters")
        String houseNumber,

        @NotBlank(message = "Street or block is required")
        @Size(max = 30, message = "Street or block cannot exceed 30 characters")
        String streetOrBlock,

        @NotNull(message = "Identification type is required (DPI, PASSPORT)")
        IdentificationType idType,

        @NotBlank(message = "Identification number is required")
        @Size(max = 30, message = "Identification number cannot exceed 30 characters")
        String idNumber,

        @NotBlank(message = "Owner email is required")
        @Email(message = "Invalid email format")
        @Size(max = 100, message = "Email cannot exceed 100 characters")
        String email,

        @NotBlank(message = "Owner full name is required")
        @Size(max = 100, message = "Full name cannot exceed 100 characters")
        String fullName
) {}
