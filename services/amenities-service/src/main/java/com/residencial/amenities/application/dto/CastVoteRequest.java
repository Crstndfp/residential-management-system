package com.residencial.amenities.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CastVoteRequest(
        @NotNull(message = "House ID cannot be null")
        UUID houseId,

        @NotBlank(message = "House number cannot be blank")
        String houseNumber,

        @NotNull(message = "Voter user ID cannot be null")
        UUID voterUserId,

        @NotBlank(message = "Vote choice cannot be blank")
        String voteChoice
) {
}
